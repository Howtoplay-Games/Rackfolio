package com.portproof.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpAvatarButton
import com.portproof.core.designsystem.component.PpEmoji
import com.portproof.core.designsystem.component.PpFeatureCard
import com.portproof.core.designsystem.component.PpHeroProgressCard
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpIllustration
import com.portproof.core.designsystem.component.PpMetricCard
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.component.PpTextLink
import com.portproof.core.designsystem.component.horizontalBleed
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.ui.LocalBottomBarInset
import com.portproof.core.ui.greetingRes
import com.portproof.core.ui.illustration
import com.portproof.core.ui.labelRes
import com.portproof.core.ui.pastelFor
import com.portproof.core.ui.R as UiR

internal data class DashboardActions(
    val onOpenActivity: () -> Unit,
    val onCreateProject: () -> Unit,
    val onOpenProject: (ProjectId) -> Unit,
    val onOpenProjects: (ProjectListFilter) -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenInsight: () -> Unit,
)

@Composable
internal fun DashboardRoute(
    actions: DashboardActions,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(state, actions)
}

/** Layout of the reference home screen. From 840dp the feature cards move into a second column. */
@Composable
internal fun DashboardScreen(state: DashboardUiState, actions: DashboardActions) {
    val spacing = PortProofTheme.spacing
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val twoColumns = maxWidth >= 840.dp
        Column(
            modifier = Modifier
                .widthIn(max = if (twoColumns) 1200.dp else spacing.maxContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = spacing.screenGutter),
        ) {
            Header(state, actions)
            if (state.isLoading) return@Column
            Spacer(Modifier.height(20.dp))
            if (twoColumns) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xxl)) {
                    Column(Modifier.weight(1f)) { ProgressAndOverview(state, actions) }
                    Column(Modifier.weight(1f)) { ActiveProjects(state, actions) }
                }
            } else {
                ProgressAndOverview(state, actions)
                Spacer(Modifier.height(spacing.sectionGap))
                ActiveProjects(state, actions)
            }
            Spacer(Modifier.height(LocalBottomBarInset.current + spacing.lg))
        }
    }
}

@Composable
private fun Header(state: DashboardUiState, actions: DashboardActions) {
    val colors = PortProofTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalBleed(4.dp)
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PpAvatarButton(state.technicianName, stringResource(R.string.dashboard_profile), actions.onOpenSettings)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(stringResource(greetingRes()), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            Text(
                // Nothing until loaded, so the fallback never flashes before the stored name.
                if (state.isLoading) "" else state.technicianName.ifEmpty { stringResource(R.string.dashboard_name_fallback) },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
        }
        PpIconButton(PortProofIcons.Bell, stringResource(R.string.dashboard_activity), actions.onOpenActivity)
    }
}

@Composable
private fun ColumnScope.ProgressAndOverview(state: DashboardUiState, actions: DashboardActions) {
    val summary = state.summary
    val percent = (summary.completionRatio * 100).toInt()
    PpHeroProgressCard(
        title = stringResource(R.string.dashboard_progress_title),
        subtitle = pluralStringResource(R.plurals.dashboard_progress_left, summary.active, summary.active),
        progress = summary.completionRatio,
        progressLabel = "$percent%",
        accessibilityLabel = pluralStringResource(R.plurals.dashboard_progress_a11y, summary.active, percent, summary.active),
        onClick = { actions.onOpenProjects(ProjectListFilter.ALL) },
    )
    Spacer(Modifier.height(PortProofTheme.spacing.sectionGap))
    PpSectionHeader(
        title = stringResource(R.string.dashboard_overview),
        action = { PpTextLink(stringResource(UiR.string.ui_see_details), actions.onOpenInsight) },
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(PortProofTheme.spacing.cardGap)) {
        PpMetricCard(
            label = stringResource(R.string.dashboard_active_projects_metric),
            emoji = PpEmoji.Tools,
            value = summary.active.toString(),
            unit = pluralStringResource(R.plurals.dashboard_unit_projects, summary.active),
            onClick = { actions.onOpenProjects(ProjectListFilter.ACTIVE) },
            modifier = Modifier.weight(1f),
        )
        PpMetricCard(
            label = stringResource(R.string.dashboard_due_this_week),
            emoji = PpEmoji.Hourglass,
            value = state.dueThisWeek.toString(),
            unit = pluralStringResource(R.plurals.dashboard_unit_projects, state.dueThisWeek),
            onClick = actions.onOpenInsight,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ColumnScope.ActiveProjects(state: DashboardUiState, actions: DashboardActions) {
    PpSectionHeader(
        title = stringResource(R.string.dashboard_active_projects),
        action = { PpTextLink(stringResource(UiR.string.ui_see_details), { actions.onOpenProjects(ProjectListFilter.ACTIVE) }) },
    )
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(PortProofTheme.spacing.cardGap)) {
        state.activeProjects.forEachIndexed { index, project ->
            val details = listOfNotNull(project.customerName, project.siteName).joinToString(" · ")
            PpFeatureCard(
                title = project.name,
                caption = details.ifEmpty { stringResource(project.status.labelRes) },
                buttonLabel = stringResource(R.string.dashboard_open_project),
                illustration = project.illustration(),
                containerColor = pastelFor(index),
                onClick = { actions.onOpenProject(project.id) },
            )
        }
        PpFeatureCard(
            title = stringResource(
                if (state.summary.isEmpty) R.string.dashboard_first_project_title else R.string.dashboard_new_project_title,
            ),
            caption = stringResource(R.string.dashboard_new_project_caption),
            buttonLabel = stringResource(R.string.dashboard_new_project_action),
            illustration = PpIllustration.Construction,
            containerColor = pastelFor(state.activeProjects.size),
            onClick = actions.onCreateProject,
        )
    }
}
