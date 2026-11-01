package com.portproof.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpIconButton
import com.portproof.core.designsystem.component.PpIconButtonStyle
import com.portproof.core.designsystem.component.PpIconTile
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.ScheduleState
import com.portproof.core.model.scheduleStateOf
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Project card in the layout of the reference's meal cards: status icon, name, customer and last
 * update on the left, the schedule figure on the right, then start, due and status with an edit
 * button.
 */
@Composable
fun ProjectCard(
    project: Project,
    today: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
) {
    val colors = PortProofTheme.colors
    PpCard(modifier = modifier, onClick = onClick, contentPadding = PaddingValues(16.dp), glow = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PpIconTile(project.status.icon, size = 48.dp, iconSize = 21.dp, containerColor = colors.accentContainer, tint = colors.onAccentContainer)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(project.name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                val customer = listOfNotNull(project.customerName, project.siteName).joinToString(" · ")
                Text(
                    customer.ifEmpty { stringResource(R.string.ui_unnamed_customer) },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.ui_updated, relativeTime(project.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(10.dp))
            ScheduleFigure(project, today)
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Stat(stringResource(R.string.ui_card_start), project.startDate?.let { shortDate(it) } ?: stringResource(R.string.ui_card_not_set))
            Stat(stringResource(R.string.ui_card_due), project.endDate?.let { shortDate(it) } ?: stringResource(R.string.ui_card_not_set))
            Stat(
                stringResource(R.string.ui_card_status),
                stringResource(if (project.isArchived) R.string.ui_archived else project.status.labelRes),
                Modifier.weight(1f),
            )
            if (onEdit != null) {
                PpIconButton(
                    PortProofIcons.Edit,
                    stringResource(R.string.ui_action_edit_project, project.name),
                    onEdit,
                    style = PpIconButtonStyle.Muted,
                    visualSize = 36.dp,
                    iconSize = 17.dp,
                )
            }
        }
    }
}

/** Big figure plus caption, like "693 kcal / 35% of goal" in the reference. */
@Composable
private fun ScheduleFigure(project: Project, today: LocalDate) {
    val colors = PortProofTheme.colors
    val schedule = scheduleStateOf(project, today)
    val (value, unit, caption, captionColor) = when {
        project.status == ProjectStatus.COMPLETED || schedule == ScheduleState.Done ->
            Figure(stringResource(R.string.ui_card_percent, 100), null, stringResource(R.string.ui_status_completed), colors.successText)
        schedule is ScheduleState.Running -> Figure(
            stringResource(R.string.ui_card_percent, (schedule.progress * 100).roundToInt()),
            null,
            stringResource(R.string.ui_card_day_of, schedule.day, schedule.totalDays),
            colors.accentText,
        )
        schedule is ScheduleState.Upcoming -> Figure(
            schedule.daysUntilStart.toString(),
            pluralStringResource(R.plurals.ui_card_days, schedule.daysUntilStart.toInt()),
            stringResource(R.string.ui_card_until_start),
            colors.textSecondary,
        )
        schedule is ScheduleState.Overdue -> Figure(
            schedule.daysOver.toString(),
            pluralStringResource(R.plurals.ui_card_days, schedule.daysOver.toInt()),
            stringResource(R.string.ui_card_overdue),
            colors.dangerText,
        )
        // Without both dates there is no schedule figure, only the reason.
        else -> Figure(null, null, stringResource(R.string.ui_no_dates), colors.textSecondary)
    }
    Column(horizontalAlignment = Alignment.End) {
        if (value != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = PortProofTheme.numbers.metric, color = colors.textPrimary)
                if (unit != null) {
                    Text(" $unit", style = PortProofTheme.numbers.metricUnit, color = colors.textPrimary, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }
        Text(caption, style = MaterialTheme.typography.labelSmall, color = captionColor, maxLines = 1)
    }
}

private data class Figure(val value: String?, val unit: String?, val caption: String, val captionColor: Color)

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = PortProofTheme.colors.textSecondary, maxLines = 1)
        Text(value, style = PortProofTheme.numbers.metricSmall, color = PortProofTheme.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun shortDate(date: LocalDate): String = PortProofFormats.dayAndMonth(date, currentLocale())
