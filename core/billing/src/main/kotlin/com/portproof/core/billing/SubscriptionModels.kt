package com.portproof.core.billing

import com.portproof.core.model.AppPlan
import java.time.Period

/**
 * The Google Play subscription PortProof sells. Create it in Play Console with this product ID and
 * two auto-renewing base plans billed every month (P1M) and every year (P1Y); the app recognises
 * them by billing period, so their IDs are free (suggested: `monthly`, `yearly`). Prices,
 * currencies and free-trial offers are configured there and read at runtime; none are hard-coded.
 */
object BillingProducts {
    const val PRO_SUBSCRIPTION = "portproof_pro"
}

/** One purchasable option on the subscription page (a base plan, with its best eligible offer). */
data class SubscriptionPlan(
    val basePlanId: String,
    val offerToken: String,
    val billingPeriod: Period,
    val formattedPrice: String,
    val priceMicros: Long,
    val currencyCode: String,
    /** Length of the free trial the user is eligible for, if any. */
    val freeTrial: Period?,
) {
    val isYearly: Boolean get() = billingPeriod.toTotalMonths() == 12L
    val isMonthly: Boolean get() = billingPeriod.toTotalMonths() == 1L
}

/** Pricing phase as reported by Google Play (ISO 8601 [billingPeriod], e.g. "P1M"). */
data class PhaseSnapshot(
    val formattedPrice: String,
    val priceMicros: Long,
    val currencyCode: String,
    val billingPeriod: String,
    val infiniteRecurring: Boolean,
)

/** A subscription offer as reported by Google Play. [offerId] is null for the base plan itself. */
data class OfferSnapshot(
    val basePlanId: String,
    val offerId: String?,
    val offerToken: String,
    val phases: List<PhaseSnapshot>,
)

object PlanSelection {

    /**
     * One plan per base plan, shortest billing period first. Google Play only returns offers the
     * user is eligible for, so a free-trial offer is preferred over the plain base plan when present.
     */
    fun select(offers: List<OfferSnapshot>): List<SubscriptionPlan> = offers
        .groupBy { it.basePlanId }
        .mapNotNull { (_, candidates) ->
            val best = candidates.firstOrNull { trialOf(it) != null }
                ?: candidates.firstOrNull { it.offerId == null }
                ?: candidates.first()
            toPlan(best)
        }
        .sortedBy { it.billingPeriod.toTotalMonths() * 31 + it.billingPeriod.days }

    /** Whole-percent saving of [yearly] over twelve months of [monthly]; null when not comparable. */
    fun yearlySavingPercent(monthly: SubscriptionPlan?, yearly: SubscriptionPlan?): Int? {
        if (monthly == null || yearly == null || monthly.currencyCode != yearly.currencyCode) return null
        val twelveMonths = monthly.priceMicros * 12
        if (twelveMonths <= 0 || yearly.priceMicros >= twelveMonths) return null
        return ((twelveMonths - yearly.priceMicros) * 100 / twelveMonths).toInt().takeIf { it > 0 }
    }

    private fun toPlan(offer: OfferSnapshot): SubscriptionPlan? {
        val recurring = offer.phases.lastOrNull { it.infiniteRecurring } ?: return null
        val period = parsePeriod(recurring.billingPeriod) ?: return null
        return SubscriptionPlan(
            basePlanId = offer.basePlanId,
            offerToken = offer.offerToken,
            billingPeriod = period,
            formattedPrice = recurring.formattedPrice,
            priceMicros = recurring.priceMicros,
            currencyCode = recurring.currencyCode,
            freeTrial = trialOf(offer),
        )
    }

    private fun trialOf(offer: OfferSnapshot): Period? =
        offer.phases.firstOrNull { it.priceMicros == 0L && !it.infiniteRecurring }?.let { parsePeriod(it.billingPeriod) }

    private fun parsePeriod(iso: String): Period? = runCatching { Period.parse(iso) }.getOrNull()?.normalized()
}

/** Why plans cannot be shown. */
enum class CatalogProblem {
    /** Google Play is missing, signed out or blocked, or this build was not installed from Google Play. */
    BILLING_UNAVAILABLE,

    /** Google Play answered but has no PortProof Pro plans for this app (not set up in Play Console). */
    PLANS_NOT_FOUND,

    /** Google Play could not be reached right now. */
    NETWORK,

    ERROR,
}

sealed interface Catalog {
    data object Loading : Catalog
    data class Unavailable(val problem: CatalogProblem) : Catalog
    data class Ready(val plans: List<SubscriptionPlan>) : Catalog
}

data class SubscriptionState(
    val plan: AppPlan = AppPlan.Free(),
    val catalog: Catalog = Catalog.Loading,
    /** A purchase is waiting for payment (for example cash at a shop). Pro starts when it completes. */
    val pendingPurchase: Boolean = false,
    /** Debug builds only: treat this device as Pro without Google Play, for testing. */
    val debugOverrideAvailable: Boolean = false,
    val debugOverride: Boolean = false,
)

sealed interface PurchaseOutcome {
    data object Purchased : PurchaseOutcome
    data object Pending : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data class Failed(val problem: CatalogProblem) : PurchaseOutcome
}
