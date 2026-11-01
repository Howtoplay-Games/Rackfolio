package com.portproof.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpBarChart
import com.portproof.core.designsystem.component.PpEmoji
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpInsightCard
import com.portproof.core.designsystem.component.PpPanel
import com.portproof.core.designsystem.component.PpProgressBar
import com.portproof.core.designsystem.component.PpRingDay
import com.portproof.core.designsystem.component.PpRingRow
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ActivityInsights
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectId
import com.portproof.core.ui.currentLocale
import com.portproof.core.ui.formatDate
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import com.portproof.core.designsystem.R as DsR

@Composable
internal fun InsightRoute(onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit, viewModel: InsightViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InsightScreen(state, onBack, onOpenProject)
}

@Composable
internal fun InsightScreen(state: InsightUiState, onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit) {
    val spacing = PortProofTheme.spacing
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = spacing.maxContentWidth).fillMaxWidth()) {
            PpTopBar(
                title = stringResource(R.string.insight_title),
                navigationIcon = { PpIconButton(PortProofIcons.Back, stringResource(DsR.string.designsystem_back), onBack) },
            )
            val insights = state.insights ?: return@Column
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screenGutter)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                verticalArrangement = Arrangement.spacedBy(spacing.cardGap),
            ) {
                Spacer(Modifier.height(4.dp))
                HourlyCard(insights)
                CompletionCard(state, insights)
                DeadlinesCard(state, onOpenProject)
                Spacer(Modifier.height(spacing.lg))
            }
        }
    }
}

@Composable
private fun PanelHeader(start: String, end: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(start, style = MaterialTheme.typography.bodyMedium, color = PortProofTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        Text(end, style = MaterialTheme.typography.bodyMedium, color = PortProofTheme.colors.textPrimary)
    }
}

@Composable
private fun HourlyCard(insights: ActivityInsights) {
    val locale = currentLocale()
    val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    val peaks = insights.peakHours()
    val peakText = peaks.sorted().joinToString { LocalTime.of(it, 0).format(timeFormat) }
    PpInsightCard(PpEmoji.Chart, stringResource(R.string.insight_activity_title), stringResource(R.string.insight_last_7_days)) {
        PpPanel {
            PanelHeader(
                stringResource(R.string.insight_today),
                pluralStringResource(R.plurals.insight_updates, insights.today, insights.today),
            )
            Spacer(Modifier.height(14.dp))
            PpBarChart(
                values = insights.byHour,
                highlighted = peaks,
                axisLabels = listOf(0, 6, 12, 18, 23).map { LocalTime.of(it, 0).format(timeFormat) },
                accessibilityLabel = if (peaks.isEmpty()) {
                    stringResource(R.string.insight_hours_none)
                } else {
                    stringResource(R.string.insight_hours_a11y, peakText)
                },
            )
        }
    }
}

@Composable
private fun CompletionCard(state: InsightUiState, insights: ActivityInsights) {
    val locale = currentLocale()
    val maxPerDay = (insights.trend.days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val days = insights.trend.days.map { day ->
        PpRingDay(
            label = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
            progress = day.count.toFloat() / maxPerDay,
        )
    }
    PpInsightCard(PpEmoji.Check, stringResource(R.string.insight_completion_title), stringResource(R.string.insight_all_projects)) {
        PpPanel {
            PanelHeader(stringResource(R.string.insight_completed), "${state.summary.completed}/${state.summary.total}")
            Spacer(Modifier.height(12.dp))
            PpProgressBar(state.summary.completionRatio)
            Spacer(Modifier.height(16.dp))
            PpRingRow(
                days = days,
                accessibilityLabel = stringResource(
                    R.string.insight_days_a11y,
                    insights.trend.days.joinToString { "${it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)} ${it.count}" },
                ),
            )
        }
    }
}

@Composable
private fun DeadlinesCard(state: InsightUiState, onOpenProject: (ProjectId) -> Unit) {
    PpInsightCard(PpEmoji.Calendar, stringResource(R.string.insight_deadlines_title), stringResource(R.string.insight_next_7_days)) {
        PpPanel {
            if (state.deadlines.isEmpty()) {
                Text(
                    stringResource(R.string.insight_no_deadlines),
                    style = MaterialTheme.typography.bodySmall,
                    color = PortProofTheme.colors.textSecondary,
                )
            }
            state.deadlines.forEach { project -> DeadlineRow(project, state.today) { onOpenProject(project.id) } }
        }
    }
}

@Composable
private fun DeadlineRow(project: Project, today: LocalDate, onClick: () -> Unit) {
    val colors = PortProofTheme.colors
    val end = project.endDate ?: return
    val (label, overdue) = when {
        end.isBefore(today) -> stringResource(R.string.insight_overdue_since, formatDate(end)) to true
        end == today -> stringResource(R.string.insight_due_today) to false
        else -> stringResource(R.string.insight_due_on, formatDate(end)) to false
    }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(project.name, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, maxLines = 1,
            overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (overdue) colors.dangerText else colors.textPrimary,
            modifier = Modifier
                .background(if (overdue) colors.dangerContainer else colors.surface, PortProofTheme.shapes.pill)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}
