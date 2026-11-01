package com.portproof.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpAddBar
import com.portproof.core.designsystem.component.PpCanvas
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpEmptyState
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpSegmentedGauge
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectId
import com.portproof.core.ui.PortProofFormats
import com.portproof.core.ui.ProjectCard
import com.portproof.core.ui.currentLocale

internal data class DashboardActions(
    /** Calendar button: open projects ordered by start date. */
    val onOpenSchedule: () -> Unit,
    /** Bell button: the activity log. */
    val onOpenActivity: () -> Unit,
    val onCreateProject: () -> Unit,
    val onOpenProject: (ProjectId) -> Unit,
    val onEditProject: (ProjectId) -> Unit,
)

@Composable
internal fun DashboardRoute(actions: DashboardActions, viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(state, actions)
}

/** Home, laid out as the reference dashboard: header, segmented gauge, wide add button, project cards. */
@Composable
internal fun DashboardScreen(state: DashboardUiState, actions: DashboardActions) {
    val spacing = PortProofTheme.spacing
    PpCanvas(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = spacing.screenGutter, end = spacing.screenGutter, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
        ) {
            item(key = "header") {
                PpTopBar(
                    title = stringResource(R.string.dashboard_title),
                    leading = { PpIconButton(PortProofIcons.Calendar, stringResource(R.string.dashboard_schedule), actions.onOpenSchedule) },
                    actions = { PpIconButton(PortProofIcons.Bell, stringResource(R.string.dashboard_activity), actions.onOpenActivity) },
                )
            }
            item(key = "gauge") { CompletionGauge(state) }
            item(key = "add") { PpAddBar(stringResource(R.string.dashboard_new_project), actions.onCreateProject) }
            if (!state.isLoading && state.recentProjects.isEmpty()) {
                item(key = "empty") {
                    PpCard(Modifier.fillMaxWidth()) {
                        PpEmptyState(
                            icon = PortProofIcons.FolderPlus,
                            title = stringResource(R.string.dashboard_empty_title),
                            message = stringResource(R.string.dashboard_empty_message),
                        )
                    }
                }
            }
            items(state.recentProjects, key = { it.id.value }) { project ->
                ProjectCard(
                    project = project,
                    today = state.today,
                    onClick = { actions.onOpenProject(project.id) },
                    onEdit = { actions.onEditProject(project.id) },
                )
            }
        }
    }
}

/** Gauge of completed projects, with the date, the count and the total, as "1250 kcal / Goal 2000 kcal". */
@Composable
private fun CompletionGauge(state: DashboardUiState) {
    val colors = PortProofTheme.colors
    val summary = state.summary
    val description = if (summary.total == 0) {
        stringResource(R.string.dashboard_gauge_empty)
    } else {
        pluralStringResource(R.plurals.dashboard_gauge_a11y, summary.total, summary.completed, summary.total)
    }
    Box(Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.Center) {
        PpSegmentedGauge(fraction = summary.completionRatio, description = description) {
            Icon(PortProofIcons.Zap, null, tint = colors.accentText, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                PortProofFormats.dayAndMonth(state.today, currentLocale()),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textPrimary,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(summary.completed.toString(), style = PortProofTheme.numbers.metricLarge, color = colors.textPrimary)
                Text(
                    " " + stringResource(R.string.dashboard_gauge_unit),
                    style = PortProofTheme.numbers.metricUnit,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
            }
            Text(
                if (summary.total == 0) {
                    stringResource(R.string.dashboard_gauge_empty)
                } else {
                    pluralStringResource(R.plurals.dashboard_gauge_goal, summary.total, summary.total)
                },
                style = MaterialTheme.typography.labelMedium,
                color = colors.accentText,
            )
        }
    }
}
