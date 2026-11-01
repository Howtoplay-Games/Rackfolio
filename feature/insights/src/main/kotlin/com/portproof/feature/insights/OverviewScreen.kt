package com.portproof.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpHighlight
import com.portproof.core.designsystem.component.PpListScreen
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.component.PpStatCard
import com.portproof.core.designsystem.component.PpTextLink
import com.portproof.core.designsystem.component.highlightColors
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ActivityInsights
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.ui.ActivityRow
import com.portproof.core.ui.currentLocale
import com.portproof.core.ui.formatDate
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs
import kotlin.math.roundToInt

internal data class OverviewActions(
    val onOpenProjects: (ProjectListFilter) -> Unit,
    val onOpenProject: (ProjectId) -> Unit,
    val onOpenActivity: () -> Unit,
)

@Composable
internal fun OverviewRoute(actions: OverviewActions, viewModel: OverviewViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OverviewScreen(state, actions)
}

@Composable
internal fun OverviewScreen(state: OverviewUiState, actions: OverviewActions) {
    val spacing = PortProofTheme.spacing
    val s = state.summary
    PpListScreen {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            PpTopBar(stringResource(R.string.overview_title))
            if (state.isLoading) return@Column
            StatRow(
                { PpStatCard(stringResource(R.string.overview_active), s.active.toString(), it, onClick = { actions.onOpenProjects(ProjectListFilter.ACTIVE) }) },
                {
                    PpStatCard(
                        stringResource(R.string.overview_completed), s.completed.toString(), it,
                        caption = stringResource(R.string.overview_of_total, s.total), filled = true,
                        onClick = { actions.onOpenProjects(ProjectListFilter.COMPLETED) },
                    )
                },
            )
            StatRow(
                { PpStatCard(stringResource(R.string.overview_planned), s.planned.toString(), it) },
                { PpStatCard(stringResource(R.string.overview_on_hold), s.onHold.toString(), it) },
            )
            StatRow(
                { PpStatCard(stringResource(R.string.overview_archived), s.archived.toString(), it, onClick = { actions.onOpenProjects(ProjectListFilter.ARCHIVED) }) },
                { state.insights?.let { insights -> UpdatesCard(insights, it) } },
            )
            Deadlines(state, actions.onOpenProject)
            RecentActivity(state, actions)
            Spacer(Modifier.height(spacing.lg))
        }
    }
}

@Composable
private fun StatRow(start: @Composable (Modifier) -> Unit, end: @Composable (Modifier) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(PortProofTheme.spacing.md)) {
        start(Modifier.weight(1f))
        end(Modifier.weight(1f))
    }
}

@Composable
private fun UpdatesCard(insights: ActivityInsights, modifier: Modifier) {
    val ratio = insights.trend.changeRatio
    val peak = insights.peakHours(count = 1).firstOrNull()
    val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(currentLocale())
    // Week-on-week change when there is a baseline, otherwise the busiest hour.
    val caption = when {
        ratio != null && ratio >= 0f -> stringResource(R.string.overview_change_up, (ratio * 100).roundToInt())
        ratio != null -> stringResource(R.string.overview_change_down, abs(ratio * 100).roundToInt())
        peak != null -> stringResource(R.string.overview_busiest, LocalTime.of(peak, 0).format(time))
        else -> stringResource(R.string.overview_no_baseline)
    }
    PpStatCard(stringResource(R.string.overview_updates), insights.trend.total.toString(), modifier, caption = caption)
}

@Composable
private fun Deadlines(state: OverviewUiState, onOpenProject: (ProjectId) -> Unit) {
    PpSectionHeader(stringResource(R.string.overview_deadlines))
    PpCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
        if (state.deadlines.isEmpty()) {
            Text(
                stringResource(R.string.overview_no_deadlines),
                style = MaterialTheme.typography.bodySmall,
                color = PortProofTheme.colors.textSecondary,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
        state.deadlines.forEach { project -> DeadlineRow(project, state.today) { onOpenProject(project.id) } }
    }
}

@Composable
private fun DeadlineRow(project: Project, today: LocalDate, onClick: () -> Unit) {
    val colors = PortProofTheme.colors
    val end = project.endDate ?: return
    val (label, highlight) = when {
        end.isBefore(today) -> stringResource(R.string.overview_overdue_since, formatDate(end)) to PpHighlight.Red
        end == today -> stringResource(R.string.overview_due_today) to PpHighlight.Orange
        else -> stringResource(R.string.overview_due_on, formatDate(end)) to PpHighlight.Yellow
    }
    val (band, ink) = highlightColors(highlight)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            project.name,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = ink,
            modifier = Modifier
                .background(band, PortProofTheme.shapes.highlight)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun RecentActivity(state: OverviewUiState, actions: OverviewActions) {
    PpSectionHeader(
        stringResource(R.string.overview_recent),
        action = { PpTextLink(stringResource(R.string.overview_see_all), actions.onOpenActivity) },
    )
    PpCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
        if (state.recentActivity.isEmpty()) {
            Text(
                stringResource(R.string.overview_no_activity),
                style = MaterialTheme.typography.bodySmall,
                color = PortProofTheme.colors.textSecondary,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
        state.recentActivity.forEach { entry ->
            val id = entry.projectId
            ActivityRow(entry, onClick = id?.let { { actions.onOpenProject(it) } })
        }
    }
}
