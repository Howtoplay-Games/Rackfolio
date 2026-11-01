package com.portproof.feature.subscription

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.billing.Catalog
import com.portproof.core.billing.CatalogProblem
import com.portproof.core.billing.PurchaseOutcome
import com.portproof.core.model.AppPlan
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.TestProjectRepository
import com.portproof.core.testing.TestSubscriptionRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class PaywallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val subscriptions = TestSubscriptionRepository()
    private val activity: Activity by lazy { Robolectric.buildActivity(Activity::class.java).get() }

    private fun paywall(handle: SavedStateHandle = SavedStateHandle()) = PaywallViewModel(handle, subscriptions, TestProjectRepository())

    @Test
    fun loadsPlansFromGooglePlayAndPreselectsYearly() = runTest {
        val vm = paywall()
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertEquals(1, subscriptions.refreshCount)
        val plans = assertIs<Catalog.Ready>(vm.uiState.value.subscription.catalog).plans
        assertEquals("yearly", vm.selectedPlan(plans)?.basePlanId)

        vm.select(plans.first { it.isMonthly })
        assertEquals("monthly", vm.selectedPlan(plans)?.basePlanId)
        vm.subscribe(activity)
        assertEquals(listOf("monthly"), subscriptions.launched.map { it.basePlanId })
    }

    @Test
    fun reportsPurchaseOutcomesButStaysQuietOnCancel() = runTest {
        val vm = paywall()
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()

        subscriptions.outcomes.emit(PurchaseOutcome.Cancelled)
        advanceUntilIdle()
        assertNull(vm.message)
        subscriptions.outcomes.emit(PurchaseOutcome.Failed(CatalogProblem.NETWORK))
        advanceUntilIdle()
        assertEquals(PaywallMessage.Failed(CatalogProblem.NETWORK), vm.message)
        subscriptions.outcomes.emit(PurchaseOutcome.Purchased)
        advanceUntilIdle()
        assertEquals(PaywallMessage.Purchased, vm.message)

        subscriptions.launchSucceeds = false
        vm.subscribe(activity)
        assertEquals(PaywallMessage.CouldNotOpen, vm.message)
    }

    @Test
    fun unavailablePlansCanBeRetriedAndTheDebugSwitchGrantsPro() = runTest {
        subscriptions.catalogOnRefresh = Catalog.Unavailable(CatalogProblem.BILLING_UNAVAILABLE)
        val vm = paywall()
        backgroundScope.launch { vm.uiState.collect() }
        advanceUntilIdle()
        assertEquals(Catalog.Unavailable(CatalogProblem.BILLING_UNAVAILABLE), vm.uiState.value.subscription.catalog)
        vm.subscribe(activity)
        assertEquals(emptyList(), subscriptions.launched)

        vm.refresh()
        advanceUntilIdle()
        assertEquals(2, subscriptions.refreshCount)

        vm.setDebugOverride(true)
        advanceUntilIdle()
        assertEquals(AppPlan.Pro, vm.uiState.value.subscription.plan)
    }
}
