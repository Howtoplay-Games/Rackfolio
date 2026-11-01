package com.portproof.core.billing

import android.app.Activity
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.portproof.core.billing.di.BillingStore
import com.portproof.core.billing.di.IsDebugBuild
import com.portproof.core.data.repository.PlanRepository
import com.portproof.core.model.AppPlan
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Google Play Billing Library 9 implementation. The last purchase check is cached on the device, so
 * the plan (and the free plan's project limit) is known offline and before Google Play answers.
 *
 * Purchases are verified by Google Play only; there is no PortProof server to verify them again.
 */
@Singleton
internal class PlayBillingSubscriptionRepository @Inject constructor(
    @ApplicationContext context: Context,
    @BillingStore private val cache: DataStore<Preferences>,
    @IsDebugBuild private val isDebugBuild: Boolean,
) : SubscriptionRepository, PlanRepository, PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val refreshLock = Mutex()
    private val catalog = MutableStateFlow<Catalog>(Catalog.Loading)
    private val pending = MutableStateFlow(false)
    private val outcomes = MutableSharedFlow<PurchaseOutcome>(extraBufferCapacity = 4)

    @Volatile
    private var productDetails: ProductDetails? = null

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    private val cached: Flow<CachedPlan> = cache.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { CachedPlan(fromPlay = it[PRO_ACTIVE] == true, debugOverride = isDebugBuild && it[DEBUG_PRO] == true) }
        .distinctUntilChanged()

    override val plan: Flow<AppPlan> = cached.map { it.plan }.distinctUntilChanged()

    override suspend fun currentPlan(): AppPlan = plan.first()

    override val state: StateFlow<SubscriptionState> = combine(cached, catalog, pending) { cachedPlan, catalog, pending ->
        SubscriptionState(
            plan = cachedPlan.plan,
            catalog = catalog,
            pendingPurchase = pending,
            debugOverrideAvailable = isDebugBuild,
            debugOverride = cachedPlan.debugOverride,
        )
    }.stateIn(scope, SharingStarted.Eagerly, SubscriptionState(debugOverrideAvailable = isDebugBuild))

    override val purchaseOutcomes: Flow<PurchaseOutcome> = outcomes.asSharedFlow()

    override suspend fun refresh() = refreshLock.withLock {
        val setup = connect()
        if (setup != BillingResponseCode.OK) {
            catalog.value = Catalog.Unavailable(problemFor(setup))
            return@withLock
        }
        loadCatalog()
        loadPurchases()
    }

    override fun launchPurchase(activity: Activity, plan: SubscriptionPlan): Boolean {
        val details = productDetails ?: return false
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(plan.offerToken)
                        .build(),
                ),
            )
            .build()
        return client.launchBillingFlow(activity, params).responseCode == BillingResponseCode.OK
    }

    override suspend fun setDebugOverride(enabled: Boolean) {
        if (isDebugBuild) cache.edit { it[DEBUG_PRO] = enabled }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        scope.launch {
            val outcome = when (result.responseCode) {
                BillingResponseCode.OK -> {
                    val ours = purchases.orEmpty().filter { BillingProducts.PRO_SUBSCRIPTION in it.products }
                    applyPurchases(ours, complete = false)
                    when {
                        ours.any { it.purchaseState == Purchase.PurchaseState.PURCHASED } -> PurchaseOutcome.Purchased
                        ours.any { it.purchaseState == Purchase.PurchaseState.PENDING } -> PurchaseOutcome.Pending
                        else -> PurchaseOutcome.Failed(CatalogProblem.ERROR)
                    }
                }
                BillingResponseCode.USER_CANCELED -> PurchaseOutcome.Cancelled
                BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    loadPurchases()
                    PurchaseOutcome.Purchased
                }
                else -> PurchaseOutcome.Failed(problemFor(result.responseCode))
            }
            outcomes.emit(outcome)
        }
    }

    /** Returns the setup response code. Gives up after [CONNECT_TIMEOUT_MS] if Play never answers. */
    private suspend fun connect(): Int {
        if (client.isReady) return BillingResponseCode.OK
        return withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                client.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (continuation.isActive) continuation.resume(result.responseCode)
                    }

                    override fun onBillingServiceDisconnected() {
                        if (continuation.isActive) continuation.resume(BillingResponseCode.SERVICE_DISCONNECTED)
                    }
                })
            }
        } ?: BillingResponseCode.SERVICE_UNAVAILABLE
    }

    private suspend fun loadCatalog() {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(BillingProducts.PRO_SUBSCRIPTION)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = client.queryProductDetails(QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build())
        val code = result.billingResult.responseCode
        val details = result.productDetailsList.orEmpty().firstOrNull { it.productId == BillingProducts.PRO_SUBSCRIPTION }
        productDetails = details
        val plans = details?.subscriptionOfferDetails.orEmpty().map { it.snapshot() }.let(PlanSelection::select)
        catalog.value = when {
            code != BillingResponseCode.OK -> Catalog.Unavailable(problemFor(code))
            plans.isEmpty() -> Catalog.Unavailable(CatalogProblem.PLANS_NOT_FOUND)
            else -> Catalog.Ready(plans)
        }
    }

    private suspend fun loadPurchases() {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        val result = client.queryPurchasesAsync(params)
        if (result.billingResult.responseCode != BillingResponseCode.OK) return
        applyPurchases(result.purchasesList.filter { BillingProducts.PRO_SUBSCRIPTION in it.products }, complete = true)
    }

    /**
     * Acknowledges new purchases (Google Play refunds unacknowledged ones after three days) and
     * stores the plan. A [complete] list replaces the cached plan; a partial update can only add Pro.
     */
    private suspend fun applyPurchases(purchases: List<Purchase>, complete: Boolean) {
        val active = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        pending.value = purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING }
        active.filterNot { it.isAcknowledged }.forEach { purchase ->
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build())
        }
        if (complete || active.isNotEmpty()) cache.edit { it[PRO_ACTIVE] = active.isNotEmpty() }
    }

    private fun ProductDetails.SubscriptionOfferDetails.snapshot() = OfferSnapshot(
        basePlanId = basePlanId,
        offerId = offerId,
        offerToken = offerToken,
        phases = pricingPhases.pricingPhaseList.map {
            PhaseSnapshot(
                formattedPrice = it.formattedPrice,
                priceMicros = it.priceAmountMicros,
                currencyCode = it.priceCurrencyCode,
                billingPeriod = it.billingPeriod,
                infiniteRecurring = it.recurrenceMode == ProductDetails.RecurrenceMode.INFINITE_RECURRING,
            )
        },
    )

    private data class CachedPlan(val fromPlay: Boolean, val debugOverride: Boolean) {
        val plan: AppPlan get() = if (fromPlay || debugOverride) AppPlan.Pro else AppPlan.Free()
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000L
        val PRO_ACTIVE = booleanPreferencesKey("pro_active")
        val DEBUG_PRO = booleanPreferencesKey("debug_pro_override")

        fun problemFor(code: Int): CatalogProblem = when (code) {
            BillingResponseCode.BILLING_UNAVAILABLE,
            BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingResponseCode.DEVELOPER_ERROR,
            -> CatalogProblem.BILLING_UNAVAILABLE
            BillingResponseCode.ITEM_UNAVAILABLE -> CatalogProblem.PLANS_NOT_FOUND
            BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingResponseCode.NETWORK_ERROR,
            BillingResponseCode.SERVICE_DISCONNECTED,
            -> CatalogProblem.NETWORK
            else -> CatalogProblem.ERROR
        }
    }
}
