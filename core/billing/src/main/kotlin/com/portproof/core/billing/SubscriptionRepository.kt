package com.portproof.core.billing

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** PortProof Pro through Google Play Billing. Purchases are handled entirely by Google Play. */
interface SubscriptionRepository {
    val state: StateFlow<SubscriptionState>

    /** Results of purchase flows started with [launchPurchase]. */
    val purchaseOutcomes: Flow<PurchaseOutcome>

    /** Connects to Google Play, loads the plans and re-checks existing purchases. */
    suspend fun refresh()

    /** Opens Google Play's purchase sheet. Returns false when it could not be opened. */
    fun launchPurchase(activity: Activity, plan: SubscriptionPlan): Boolean

    /** Debug builds only; ignored otherwise. */
    suspend fun setDebugOverride(enabled: Boolean)
}
