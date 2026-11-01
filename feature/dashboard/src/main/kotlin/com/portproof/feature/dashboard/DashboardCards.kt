package com.portproof.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpDelta
import com.portproof.core.designsystem.component.PpIconTile
import com.portproof.core.designsystem.component.PpPrimaryButton
import com.portproof.core.designsystem.component.PpQuickAction
import com.portproof.core.designsystem.component.PpStatCard
import com.portproof.core.designsystem.component.PpTone
import com.portproof.core.designsystem.component.PpTrend
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSummary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.semantics
import com.portproof.core.designsystem.component.PpChartPoint
import com.portproof.core.designsystem.component.PpChipButton
import com.portproof.core.designsystem.component.PpLineChart
import com.portproof.core.designsystem.component.PpSectionHeader
import com.portproof.core.designsystem.component.PpSemiCircleGauge
import com.portproof.core.model.ActivityTrend
import com.portproof.core.model.ProjectStatus
import com.portproof.core.ui.ActivityRow
import com.portproof.core.ui.PortProofFormats
import com.portproof.core.ui.ProjectCard
import com.portproof.core.ui.currentLocale
import com.portproof.core.ui.icon
import com.portproof.core.ui.labelRes
import kotlin.math.roundToInt
import com.portproof.core.ui.R as UiR

/** Stat cards. On phones they scroll horizontally (as in the reference design); wider screens fit all three. */
@Composable
internal fun OverviewSection(state: DashboardUiState, actions: DashboardActions, scrollable: Boolean) {
    val summary = state.summary
    SectionTitle(stringResource(R.string.dashboard_overview), Modifier.padding(top = 8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (scrollable) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val itemModifier = if (scrollable) Modifier.width(168.dp) else Modifier.weight(1f)
        PpStatCard(
            icon = PortProofIcons.Folder,
            label = stringResource(R.string.dashboard_total_projects),
            value = summary.total.toString(),
            onClick = { actions.onOpenProjects(ProjectListFilter.ALL) },
            modifier = itemModifier,
            delta = { MonthDelta(summary.createdThisMonth, stringResource(R.string.dashboard_new_this_month)) },
        )
        PpStatCard(
            icon = PortProofIcons.Activity,
            label = stringResource(R.string.dashboard_active_projects),
            value = summary.active.toString(),
            onClick = { actions.onOpenProjects(ProjectListFilter.ACTIVE) },
            modifier = itemModifier,
            delta = {
                PpDelta(summary.inProgress.toString(), stringResource(R.string.dashboard_in_progress), PpTrend.Flat, PpTone.Accent)
            },
        )
        PpStatCard(
            icon = PortProofIcons.CircleCheck,
            label = stringResource(R.string.dashboard_completed_projects),
            value = summary.completed.toString(),
            onClick = { actions.onOpenProjects(ProjectListFilter.COMPLETED) },
            modifier = itemModifier,
            delta = { MonthDelta(summary.completedThisMonth, stringResource(R.string.dashboard_completed_this_month)) },
        )
    }
}

@Composable
private fun MonthDelta(count: Int, caption: String) {
    if (count > 0) {
        PpDelta("+$count", caption, PpTrend.Up, PpTone.Success)
    } else {
        PpDelta("0", caption, PpTrend.Flat, PpTone.Neutral)
    }
}

@Composable
internal fun QuickActionsCard(actions: DashboardActions) {
    PpCard(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)) {
        Text(
            stringResource(R.string.dashboard_quick_actions),
            style = MaterialTheme.typography.titleSmall,
            color = PortProofTheme.colors.textPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PpQuickAction(PortProofIcons.Add, stringResource(R.string.dashboard_action_new), actions.onCreateProject, emphasized = true)
            PpQuickAction(PortProofIcons.Search, stringResource(R.string.dashboard_action_search), actions.onSearch)
            PpQuickAction(PortProofIcons.Activity, stringResource(R.string.dashboard_action_active), onClick = {
                actions.onOpenProjects(ProjectListFilter.ACTIVE)
            })
            PpQuickAction(PortProofIcons.CircleCheck, stringResource(R.string.dashboard_action_completed), onClick = {
                actions.onOpenProjects(ProjectListFilter.COMPLETED)
            })
            PpQuickAction(PortProofIcons.Archive, stringResource(R.string.dashboard_action_archived), onClick = {
                actions.onOpenProjects(ProjectListFilter.ARCHIVED)
            })
        }
    }
}

@Composable
internal fun WelcomeCard(onCreateProject: () -> Unit) {
    val colors = PortProofTheme.colors
    PpCard(modifier = Modifier.padding(top = 8.dp), contentPadding = PaddingValues(20.dp)) {
        PpIconTile(PortProofIcons.EthernetPort, size = 48.dp, iconSize = 24.dp, containerColor = colors.accentContainer, tint = colors.onAccentContainer)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.dashboard_welcome_title), style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.dashboard_welcome_message), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        Spacer(Modifier.height(20.dp))
        PpPrimaryButton(
            text = stringResource(R.string.dashboard_welcome_action),
            onClick = onCreateProject,
            leadingIcon = PortProofIcons.Add,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Completion gauge plus the breakdown of open projects (maps the reference "health" card). */
@Composable
internal fun StatusCard(summary: ProjectSummary, onViewAll: () -> Unit) {
    val colors = PortProofTheme.colors
    val percent = (summary.completionRatio * 100).toInt()
    PpCard {
        PpSectionHeader(
            title = stringResource(R.string.dashboard_status_title),
            style = MaterialTheme.typography.titleSmall,
            action = {
                PpChipButton(
                    stringResource(UiR.string.ui_action_view_all),
                    onViewAll,
                )
            },
        )
        PpSemiCircleGauge(
            progress = summary.completionRatio,
            value = "$percent%",
            caption = stringResource(R.string.dashboard_status_completed),
            accessibilityLabel = pluralStringResource(R.plurals.dashboard_status_gauge_a11y, summary.total, summary.completed, summary.total),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 300.dp)
                .padding(horizontal = 36.dp, vertical = 8.dp),
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            StatusColumn(ProjectStatus.PLANNED, summary.planned, Modifier.weight(1f))
            ColumnDivider()
            StatusColumn(ProjectStatus.IN_PROGRESS, summary.inProgress, Modifier.weight(1f))
            ColumnDivider()
            StatusColumn(ProjectStatus.ON_HOLD, summary.onHold, Modifier.weight(1f))
        }
        if (summary.archived > 0) {
            Spacer(Modifier.height(12.dp))
            Text(
                pluralStringResource(R.plurals.dashboard_archived_count, summary.archived, summary.archived),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
            )
        }
    }
}

@Composable
private fun StatusColumn(status: ProjectStatus, count: Int, modifier: Modifier = Modifier) {
    val colors = PortProofTheme.colors
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PpIconTile(status.icon, size = 28.dp, iconSize = 15.dp)
        Text(stringResource(status.labelRes), style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        Text(count.toString(), style = PortProofTheme.numbers.metricSmall, color = colors.textPrimary)
    }
}

@Composable
private fun ColumnDivider() {
    Box(
        Modifier
            .padding(vertical = 4.dp)
            .width(1.dp)
            .height(64.dp)
            .background(PortProofTheme.colors.divider),
    )
}

@Composable
internal fun ActivityTrendCard(trend: ActivityTrend) {
    val colors = PortProofTheme.colors
    val locale = currentLocale()
    val points = trend.days.map { day ->
        PpChartPoint(
            label = PortProofFormats.dayOfWeekShort(day.date, locale),
            value = day.count,
            tooltip = pluralStringResource(R.plurals.dashboard_activity_updates, day.count, day.count) +
                " · " + PortProofFormats.dayAndMonth(day.date, locale),
        )
    }
    val summary = trend.days.joinToString { "${PortProofFormats.dayOfWeekShort(it.date, locale)} ${it.count}" }
    PpCard {
        PpSectionHeader(
            title = stringResource(R.string.dashboard_activity_title),
            style = MaterialTheme.typography.titleSmall,
            action = { PeriodLabel(stringResource(R.string.dashboard_activity_period)) },
        )
        Text(
            pluralStringResource(R.plurals.dashboard_activity_updates, trend.total, trend.total),
            style = PortProofTheme.numbers.metric,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(6.dp))
        TrendDelta(trend)
        Spacer(Modifier.height(12.dp))
        PpLineChart(points, stringResource(R.string.dashboard_activity_chart_a11y, summary))
    }
}

@Composable
private fun TrendDelta(trend: ActivityTrend) {
    val ratio = trend.changeRatio
    if (ratio == null) {
        Text(
            stringResource(R.string.dashboard_activity_no_baseline),
            style = MaterialTheme.typography.labelSmall,
            color = PortProofTheme.colors.textMuted,
        )
        return
    }
    val percent = kotlin.math.abs(ratio * 100).roundToInt()
    when {
        ratio > 0f -> PpDelta("$percent%", stringResource(R.string.dashboard_activity_vs_previous), PpTrend.Up, PpTone.Success)
        ratio < 0f -> PpDelta("$percent%", stringResource(R.string.dashboard_activity_vs_previous), PpTrend.Down, PpTone.Danger)
        else -> PpDelta("0%", stringResource(R.string.dashboard_activity_vs_previous), PpTrend.Flat, PpTone.Neutral)
    }
}

/** Static period label (not a control): the dashboard always shows the last 7 days. */
@Composable
private fun PeriodLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = PortProofTheme.colors.textSecondary,
        modifier = Modifier
            .background(PortProofTheme.colors.surfaceMuted, PortProofTheme.shapes.control)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
internal fun RecentSections(state: DashboardUiState, actions: DashboardActions) {
    val colors = PortProofTheme.colors
    PpSectionHeader(
        title = stringResource(R.string.dashboard_recent_projects),
        action = { PpChipButton(stringResource(UiR.string.ui_action_view_all), { actions.onOpenProjects(ProjectListFilter.ALL) }) },
    )
    if (state.recentProjects.isEmpty()) {
        Text(stringResource(R.string.dashboard_no_recent_projects), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    }
    state.recentProjects.forEach { project -> ProjectCard(project, onClick = { actions.onOpenProject(project.id) }) }

    PpSectionHeader(title = stringResource(R.string.dashboard_recent_activity))
    PpCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
        if (state.recentActivity.isEmpty()) {
            Text(
                stringResource(R.string.dashboard_no_activity),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }
        state.recentActivity.forEach { entry ->
            val projectId = entry.projectId
            ActivityRow(entry, onClick = projectId?.let { id -> { actions.onOpenProject(id) } })
        }
    }
}
