package com.portproof.feature.subscription

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.billing.BillingProducts
import com.portproof.core.billing.Catalog
import com.portproof.core.billing.CatalogProblem
import com.portproof.core.billing.PlanSelection
import com.portproof.core.billing.SubscriptionPlan
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpChoiceCard
import com.portproof.core.designsystem.component.PpCountBadge
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpIconTile
import com.portproof.core.designsystem.component.PpListScreen
import com.portproof.core.designsystem.component.PpPrimaryButton
import com.portproof.core.designsystem.component.PpSecondaryButton
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.component.PpTextLink
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.AppPlan
import com.portproof.core.model.PlanRules

@Composable
internal fun PaywallRoute(onClose: () -> Unit, viewModel: PaywallViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val texts = mapOf(
        PaywallMessage.Purchased to stringResource(R.string.pro_msg_purchased),
        PaywallMessage.Pending to stringResource(R.string.pro_msg_pending),
        PaywallMessage.CouldNotOpen to stringResource(R.string.pro_msg_could_not_open),
    )
    val failedTexts = CatalogProblem.entries.associateWith { stringResource(it.messageRes) }
    LaunchedEffect(viewModel.message) {
        val message = viewModel.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(if (message is PaywallMessage.Failed) failedTexts.getValue(message.problem) else texts.getValue(message))
        viewModel.onMessageShown()
    }
    Box(Modifier.fillMaxSize()) {
        PaywallScreen(
            state = state,
            selected = (state.subscription.catalog as? Catalog.Ready)?.plans?.let(viewModel::selectedPlan),
            refreshing = viewModel.isRefreshing,
            onClose = onClose,
            onSelect = viewModel::select,
            onSubscribe = { activity?.let(viewModel::subscribe) },
            onRefresh = viewModel::refresh,
            onDebugOverride = viewModel::setDebugOverride,
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 16.dp))
    }
}

@Composable
internal fun PaywallScreen(
    state: PaywallUiState,
    selected: SubscriptionPlan?,
    refreshing: Boolean,
    onClose: () -> Unit,
    onSelect: (SubscriptionPlan) -> Unit,
    onSubscribe: () -> Unit,
    onRefresh: () -> Unit,
    onDebugOverride: (Boolean) -> Unit,
) {
    val colors = PortProofTheme.colors
    val spacing = PortProofTheme.spacing
    val subscription = state.subscription
    val isPro = subscription.plan == AppPlan.Pro
    val limit = PlanRules.FREE_ACTIVE_PROJECT_LIMIT
    PpListScreen {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = spacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
        ) {
            PpTopBar(
                title = null,
                leading = { PpIconButton(PortProofIcons.Close, stringResource(R.string.pro_close), onClose) },
            )
            Hero(
                subtitle = if (isPro) {
                    stringResource(R.string.pro_subtitle_pro)
                } else {
                    pluralStringResource(R.plurals.pro_subtitle_free, limit, state.activeProjects.coerceAtMost(limit), limit)
                },
            )
            PpCard {
                Benefit(PortProofIcons.Infinity, stringResource(R.string.pro_benefit_unlimited_title), pluralStringResource(R.plurals.pro_benefit_unlimited_body, limit, limit))
                Spacer(Modifier.height(14.dp))
                Benefit(PortProofIcons.WifiOff, stringResource(R.string.pro_benefit_offline_title), stringResource(R.string.pro_benefit_offline_body))
                Spacer(Modifier.height(14.dp))
                Benefit(PortProofIcons.BadgeCheck, stringResource(R.string.pro_benefit_cancel_title), stringResource(R.string.pro_benefit_cancel_body))
            }
            if (subscription.pendingPurchase) {
                Notice(stringResource(R.string.pro_pending))
            }
            if (isPro) {
                ActiveCard()
            } else {
                Plans(subscription.catalog, selected, refreshing, onSelect, onSubscribe, onRefresh)
            }
            if (subscription.debugOverrideAvailable) DebugCard(subscription.debugOverride, onDebugOverride)
            Spacer(Modifier.height(spacing.lg))
        }
    }
}

@Composable
private fun Hero(subtitle: String) {
    val colors = PortProofTheme.colors
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(84.dp)
                .background(colors.accent, PortProofTheme.shapes.tile),
            contentAlignment = Alignment.Center,
        ) {
            Icon(PortProofIcons.Crown, null, tint = colors.onAccent, modifier = Modifier.size(40.dp))
        }
        Text(
            stringResource(R.string.pro_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 16.dp).semantics { heading() },
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, start = 16.dp, end = 16.dp),
        )
    }
}

@Composable
private fun Benefit(icon: ImageVector, title: String, body: String) {
    val colors = PortProofTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        PpIconTile(icon, size = 40.dp, iconSize = 18.dp, containerColor = colors.accentContainer, tint = colors.onAccentContainer)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
            Text(body, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
    }
}

@Composable
private fun ColumnScope.Plans(
    catalog: Catalog,
    selected: SubscriptionPlan?,
    refreshing: Boolean,
    onSelect: (SubscriptionPlan) -> Unit,
    onSubscribe: () -> Unit,
    onRefresh: () -> Unit,
) {
    val colors = PortProofTheme.colors
    PpSectionHeader(stringResource(R.string.pro_plans))
    when (catalog) {
        Catalog.Loading -> PpCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(Modifier.size(20.dp), color = colors.accentText, strokeWidth = 2.dp)
                Text(stringResource(R.string.pro_loading), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }
        is Catalog.Unavailable -> PpCard(Modifier.fillMaxWidth()) {
            Text(stringResource(catalog.problem.messageRes), style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Spacer(Modifier.height(12.dp))
            PpSecondaryButton(stringResource(R.string.pro_try_again), onRefresh, enabled = !refreshing)
        }
        is Catalog.Ready -> {
            val monthly = catalog.plans.firstOrNull { it.isMonthly }
            val yearly = catalog.plans.firstOrNull { it.isYearly }
            val saving = PlanSelection.yearlySavingPercent(monthly, yearly)
            catalog.plans.forEach { plan ->
                PlanChoice(plan, selected = plan == selected, saving = saving.takeIf { plan.isYearly }) { onSelect(plan) }
            }
            PpPrimaryButton(
                text = stringResource(if (selected?.freeTrial != null) R.string.pro_start_trial else R.string.pro_subscribe),
                onClick = onSubscribe,
                enabled = selected != null,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                listOfNotNull(
                    stringResource(if (selected?.isYearly == true) R.string.pro_fine_print_year else R.string.pro_fine_print_month),
                    selected?.freeTrial?.let { stringResource(R.string.pro_fine_print_trial) },
                ).joinToString(" "),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            )
            PpTextLink(stringResource(R.string.pro_restore), onRefresh, Modifier.align(Alignment.CenterHorizontally), color = colors.accentText)
        }
    }
}

@Composable
private fun PlanChoice(plan: SubscriptionPlan, selected: Boolean, saving: Int?, onClick: () -> Unit) {
    val colors = PortProofTheme.colors
    PpChoiceCard(selected = selected, onClick = onClick) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(
                        when {
                            plan.isMonthly -> R.string.pro_plan_monthly
                            plan.isYearly -> R.string.pro_plan_yearly
                            else -> R.string.pro_plan_other
                        },
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                )
                if (saving != null) PpCountBadge(stringResource(R.string.pro_saving, saving), Modifier.padding(start = 8.dp))
            }
            val trial = plan.freeTrial
            if (trial != null) {
                Text(
                    if (trial.toTotalMonths() == 0L) {
                        pluralStringResource(R.plurals.pro_trial_days, trial.days, trial.days)
                    } else {
                        stringResource(R.string.pro_trial_period)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.accentText,
                )
            }
        }
        Text(
            stringResource(if (plan.isYearly) R.string.pro_price_year else R.string.pro_price_month, plan.formattedPrice),
            style = PortProofTheme.numbers.metricSmall,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun ActiveCard() {
    val colors = PortProofTheme.colors
    val uriHandler = LocalUriHandler.current
    val packageName = LocalContext.current.packageName
    PpCard(Modifier.fillMaxWidth(), glow = true) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PpIconTile(PortProofIcons.BadgeCheck, size = 44.dp, iconSize = 20.dp, containerColor = colors.successContainer, tint = colors.successText)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.pro_active_title), style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Text(stringResource(R.string.pro_active_body), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
        Spacer(Modifier.height(12.dp))
        PpPrimaryButton(
            stringResource(R.string.pro_manage),
            onClick = {
                runCatching {
                    uriHandler.openUri("https://play.google.com/store/account/subscriptions?sku=${BillingProducts.PRO_SUBSCRIPTION}&package=$packageName")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Notice(text: String) {
    val colors = PortProofTheme.colors
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.warningText,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.warningContainer, PortProofTheme.shapes.panel)
            .padding(14.dp),
    )
}

@Composable
private fun DebugCard(enabled: Boolean, onChange: (Boolean) -> Unit) {
    val colors = PortProofTheme.colors
    PpCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.pro_debug_title), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.pro_debug_switch), style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary, modifier = Modifier.weight(1f))
            Switch(
                checked = enabled,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(checkedTrackColor = colors.accent, checkedThumbColor = colors.onAccent),
            )
        }
        Text(stringResource(R.string.pro_debug_body), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    }
}

private val CatalogProblem.messageRes: Int
    get() = when (this) {
        CatalogProblem.BILLING_UNAVAILABLE -> R.string.pro_unavailable_billing
        CatalogProblem.PLANS_NOT_FOUND -> R.string.pro_unavailable_plans
        CatalogProblem.NETWORK -> R.string.pro_unavailable_network
        CatalogProblem.ERROR -> R.string.pro_unavailable_error
    }
