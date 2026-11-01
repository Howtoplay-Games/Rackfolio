package com.portproof.feature.subscription

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.billing.Catalog
import com.portproof.core.billing.CatalogProblem
import com.portproof.core.billing.PurchaseOutcome
import com.portproof.core.billing.SubscriptionPlan
import com.portproof.core.billing.SubscriptionRepository
import com.portproof.core.billing.SubscriptionState
import com.portproof.core.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val subscription: SubscriptionState = SubscriptionState(),
    val activeProjects: Int = 0,
)

sealed interface PaywallMessage {
    data object Purchased : PaywallMessage
    data object Pending : PaywallMessage
    data object CouldNotOpen : PaywallMessage
    data class Failed(val problem: CatalogProblem) : PaywallMessage
}

/** PortProof Pro subscription page. Prices and trials come from Google Play at runtime. */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val subscriptions: SubscriptionRepository,
    projectRepository: ProjectRepository,
) : ViewModel() {

    val uiState: StateFlow<PaywallUiState> = combine(subscriptions.state, projectRepository.observeSummary().map { it.active }) { state, active ->
        PaywallUiState(state, active)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaywallUiState(subscriptions.state.value))

    /** The chosen base plan; defaults to yearly when Google Play offers it. */
    var selectedPlanId by mutableStateOf(savedStateHandle.get<String>(KEY_PLAN))
        private set
    var message by mutableStateOf<PaywallMessage?>(null)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    init {
        refresh()
        viewModelScope.launch {
            subscriptions.purchaseOutcomes.collect { outcome ->
                message = when (outcome) {
                    PurchaseOutcome.Purchased -> PaywallMessage.Purchased
                    PurchaseOutcome.Pending -> PaywallMessage.Pending
                    PurchaseOutcome.Cancelled -> null
                    is PurchaseOutcome.Failed -> PaywallMessage.Failed(outcome.problem)
                }
            }
        }
    }

    /** Plan to buy: the selection, else yearly, else the first plan offered. */
    fun selectedPlan(plans: List<SubscriptionPlan>): SubscriptionPlan? =
        plans.firstOrNull { it.basePlanId == selectedPlanId } ?: plans.firstOrNull { it.isYearly } ?: plans.firstOrNull()

    fun select(plan: SubscriptionPlan) {
        selectedPlanId = plan.basePlanId
        savedStateHandle[KEY_PLAN] = plan.basePlanId
    }

    /** Opens Google Play's purchase sheet for the selected plan. */
    fun subscribe(activity: Activity) {
        val plans = (uiState.value.subscription.catalog as? Catalog.Ready)?.plans ?: return
        val plan = selectedPlan(plans) ?: return
        if (!subscriptions.launchPurchase(activity, plan)) message = PaywallMessage.CouldNotOpen
    }

    /** Reloads plans and re-checks purchases ("Restore purchases" and "Try again"). */
    fun refresh() {
        if (isRefreshing) return
        isRefreshing = true
        viewModelScope.launch {
            try {
                subscriptions.refresh()
            } finally {
                isRefreshing = false
            }
        }
    }

    fun setDebugOverride(enabled: Boolean) {
        viewModelScope.launch { subscriptions.setDebugOverride(enabled) }
    }

    fun onMessageShown() {
        message = null
    }

    private companion object {
        const val KEY_PLAN = "paywall.plan"
    }
}
