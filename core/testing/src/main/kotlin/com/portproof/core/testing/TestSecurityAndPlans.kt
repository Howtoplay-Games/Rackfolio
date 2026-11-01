package com.portproof.core.testing

import android.app.Activity
import com.portproof.core.billing.Catalog
import com.portproof.core.billing.PurchaseOutcome
import com.portproof.core.billing.SubscriptionPlan
import com.portproof.core.billing.SubscriptionRepository
import com.portproof.core.billing.SubscriptionState
import com.portproof.core.data.repository.AppLockRepository
import com.portproof.core.data.repository.PlanRepository
import com.portproof.core.model.AppLockSettings
import com.portproof.core.model.AppPlan
import com.portproof.core.model.LockoutPolicy
import com.portproof.core.model.PinCheck
import com.portproof.core.model.PinRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.Period

class TestPlanRepository(initial: AppPlan = AppPlan.Free()) : PlanRepository {
    val current = MutableStateFlow(initial)
    override val plan: Flow<AppPlan> = current
    override suspend fun currentPlan(): AppPlan = current.value
}

/** In-memory [AppLockRepository] following the real lockout policy (no hashing). */
class TestAppLockRepository(private val clock: Clock, pin: String? = null) : AppLockRepository {
    private var storedPin: String? = pin
    private val state = MutableStateFlow(AppLockSettings.None.copy(pinSet = pin != null))
    override val settings: Flow<AppLockSettings> = state

    override suspend fun setPin(pin: String) {
        require(PinRules.validate(pin) == null)
        storedPin = pin
        state.update { it.copy(pinSet = true, failedAttempts = 0, lockedUntil = null) }
    }

    override suspend fun checkPin(pin: String): PinCheck {
        val now = clock.instant()
        state.value.lockedUntil?.let { if (now.isBefore(it)) return PinCheck.LockedOut(it) }
        if (pin == storedPin) {
            clearFailedAttempts()
            return PinCheck.Correct
        }
        val failed = state.value.failedAttempts + 1
        val wait = LockoutPolicy.waitAfter(failed)
        val until = wait?.let { now.plus(it) }
        state.update { it.copy(failedAttempts = failed, lockedUntil = until) }
        return if (until != null) PinCheck.LockedOut(until) else PinCheck.Wrong(LockoutPolicy.FREE_ATTEMPTS - failed)
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) = state.update { it.copy(biometricEnabled = enabled) }

    override suspend fun clearFailedAttempts() = state.update { it.copy(failedAttempts = 0, lockedUntil = null) }
}

/** [SubscriptionRepository] whose catalog and outcomes are set by the test. */
class TestSubscriptionRepository(initial: SubscriptionState = SubscriptionState()) : SubscriptionRepository, PlanRepository {
    val mutableState = MutableStateFlow(initial)
    override val state: StateFlow<SubscriptionState> = mutableState
    val outcomes = MutableSharedFlow<PurchaseOutcome>(extraBufferCapacity = 4)
    override val purchaseOutcomes: Flow<PurchaseOutcome> = outcomes
    val launched = mutableListOf<SubscriptionPlan>()
    var launchSucceeds = true
    var refreshCount = 0

    /** Catalog that [refresh] publishes; defaults to the two sample plans. */
    var catalogOnRefresh: Catalog = Catalog.Ready(listOf(sampleMonthlyPlan, sampleYearlyPlan))

    override val plan: Flow<AppPlan> = mutableState.map { it.plan }
    override suspend fun currentPlan(): AppPlan = plan.first()

    override suspend fun refresh() {
        refreshCount++
        mutableState.update { it.copy(catalog = catalogOnRefresh) }
    }

    override fun launchPurchase(activity: Activity, plan: SubscriptionPlan): Boolean {
        launched += plan
        return launchSucceeds
    }

    override suspend fun setDebugOverride(enabled: Boolean) = mutableState.update {
        it.copy(debugOverride = enabled, plan = if (enabled) AppPlan.Pro else AppPlan.Free())
    }

    companion object {
        val sampleMonthlyPlan = SubscriptionPlan(
            basePlanId = "monthly", offerToken = "token-monthly", billingPeriod = Period.ofMonths(1),
            formattedPrice = "€4.99", priceMicros = 4_990_000, currencyCode = "EUR", freeTrial = Period.ofDays(7),
        )
        val sampleYearlyPlan = SubscriptionPlan(
            basePlanId = "yearly", offerToken = "token-yearly", billingPeriod = Period.ofYears(1),
            formattedPrice = "€39.99", priceMicros = 39_990_000, currencyCode = "EUR", freeTrial = null,
        )
    }
}

/** A clock tests move forward explicitly. */
class MutableTestClock(var now: java.time.Instant = TestInstant) : Clock() {
    override fun getZone(): java.time.ZoneId = java.time.ZoneOffset.UTC
    override fun withZone(zone: java.time.ZoneId): Clock = this
    override fun instant(): java.time.Instant = now
}
