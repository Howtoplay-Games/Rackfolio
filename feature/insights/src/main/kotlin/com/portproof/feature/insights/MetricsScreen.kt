package com.portproof.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.portproof.core.designsystem.component.PpAreaChartCard
import com.portproof.core.designsystem.component.PpAreaPoint
import com.portproof.core.designsystem.component.PpEmoji
import com.portproof.core.designsystem.component.PpMetricCard
import com.portproof.core.designsystem.component.PpTopBar
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.ui.LocalBottomBarInset
import com.portproof.core.ui.PortProofFormats
import com.portproof.core.ui.currentLocale
import java.text.NumberFormat

@Composable
internal fun MetricsRoute(onOpenProjects: (ProjectListFilter) -> Unit, viewModel: MetricsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MetricsScreen(state, onOpenProjects)
}

private data class Metric(val labelRes: Int, val emoji: PpEmoji, val value: Int, val filter: ProjectListFilter?)

@Composable
internal fun MetricsScreen(state: MetricsUiState, onOpenProjects: (ProjectListFilter) -> Unit) {
    val spacing = PortProofTheme.spacing
    var showInfo by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = spacing.maxContentWidth).fillMaxWidth()) {
            PpTopBar(title = stringResource(R.string.metrics_title))
            if (state.isLoading) return@Column
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.screenGutter),
            ) {
                state.insights?.let { insights ->
                    val locale = currentLocale()
                    val average = NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 1 }.format(insights.dailyAverage)
                    val points = insights.trend.days.map { PpAreaPoint(PortProofFormats.dayOfWeekShort(it.date, locale), it.count) }
                    PpAreaChartCard(
                        caption = stringResource(R.string.metrics_average),
                        value = average,
                        unit = stringResource(R.string.metrics_unit_per_day),
                        points = points,
                        accessibilityLabel = stringResource(
                            R.string.metrics_chart_a11y,
                            points.joinToString { "${it.axisLabel} ${it.value}" },
                        ),
                        infoLabel = stringResource(R.string.metrics_info),
                        onInfoClick = { showInfo = true },
                    )
                }
                Spacer(Modifier.height(spacing.lg))
                MetricsGrid(state, onOpenProjects)
                Spacer(Modifier.height(LocalBottomBarInset.current + spacing.lg))
            }
        }
    }
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            containerColor = PortProofTheme.colors.surface,
            title = { Text(stringResource(R.string.metrics_info_title), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(R.string.metrics_info_message), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text(stringResource(R.string.metrics_info_ok)) } },
        )
    }
}

@Composable
private fun MetricsGrid(state: MetricsUiState, onOpenProjects: (ProjectListFilter) -> Unit) {
    val s = state.summary
    val metrics = listOf(
        Metric(R.string.metrics_total, PpEmoji.Folder, s.total, ProjectListFilter.ALL),
        Metric(R.string.metrics_active, PpEmoji.Tools, s.active, ProjectListFilter.ACTIVE),
        Metric(R.string.metrics_completed, PpEmoji.Check, s.completed, ProjectListFilter.COMPLETED),
        Metric(R.string.metrics_planned, PpEmoji.Calendar, s.planned, null),
        Metric(R.string.metrics_on_hold, PpEmoji.Pause, s.onHold, null),
        Metric(R.string.metrics_archived, PpEmoji.Archive, s.archived, ProjectListFilter.ARCHIVED),
    )
    Column(verticalArrangement = Arrangement.spacedBy(PortProofTheme.spacing.cardGap)) {
        metrics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(PortProofTheme.spacing.cardGap)) {
                row.forEach { metric ->
                    PpMetricCard(
                        label = stringResource(metric.labelRes),
                        emoji = metric.emoji,
                        value = metric.value.toString(),
                        unit = pluralStringResource(R.plurals.metrics_unit_projects, metric.value),
                        onClick = metric.filter?.let { filter -> { onOpenProjects(filter) } },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
