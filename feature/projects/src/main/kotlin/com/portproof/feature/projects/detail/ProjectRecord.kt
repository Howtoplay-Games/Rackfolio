package com.portproof.feature.projects.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.portproof.core.designsystem.component.PpDocSection
import com.portproof.core.designsystem.component.PpDocumentCard
import com.portproof.core.designsystem.component.PpHighlight
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.model.Project
import com.portproof.core.ui.formatDate
import com.portproof.core.ui.labelRes
import com.portproof.core.ui.relativeTime
import com.portproof.feature.projects.R
import java.time.ZoneId

/** The project shown as a document; lines that need attention carry a highlight band. */
@Composable
internal fun ProjectRecord(state: ProjectDetailUiState.Success) {
    val project = state.project
    val review = state.review
    val zone = ZoneId.systemDefault()
    val sections = listOf(
        PpDocSection(
            stringResource(R.string.project_label_customer),
            project.customerName ?: stringResource(R.string.project_review_no_customer),
            if (review.missingCustomer) PpHighlight.Yellow else PpHighlight.None,
        ),
        PpDocSection(
            stringResource(R.string.project_label_site),
            listOfNotNull(project.siteName, project.siteAddress).joinToString("\n").ifEmpty { stringResource(R.string.project_review_no_site) },
            if (review.missingSite) PpHighlight.Yellow else PpHighlight.None,
        ),
        scheduleSection(project, review.schedule),
        PpDocSection(
            stringResource(R.string.project_section_status),
            when {
                review.archived -> stringResource(R.string.project_review_archived, formatDate(project.archivedAt!!.atZone(zone).toLocalDate()))
                review.onHold -> stringResource(R.string.project_review_on_hold)
                else -> stringResource(project.status.labelRes)
            },
            when {
                review.archived -> PpHighlight.Yellow
                review.onHold -> PpHighlight.Orange
                else -> PpHighlight.None
            },
        ),
        PpDocSection(
            stringResource(R.string.project_section_description),
            project.description ?: stringResource(R.string.project_no_description),
        ),
    )
    PpDocumentCard(
        icon = PortProofIcons.FileText,
        title = project.reference ?: stringResource(R.string.project_record),
        meta = stringResource(
            R.string.project_meta,
            formatDate(project.createdAt.atZone(zone).toLocalDate()),
            relativeTime(project.updatedAt),
        ),
        heading = stringResource(R.string.project_record),
        sections = sections,
    )
}

@Composable
private fun scheduleSection(project: Project, check: ScheduleCheck): PpDocSection {
    val start = project.startDate?.let { formatDate(it) }
    val end = project.endDate?.let { formatDate(it) }
    val dates = when {
        start != null && end != null -> stringResource(R.string.project_dates_range, start, end)
        start != null -> stringResource(R.string.project_dates_start, start)
        end != null -> stringResource(R.string.project_dates_end, end)
        else -> ""
    }
    val title = stringResource(R.string.project_section_schedule)
    return when (check) {
        ScheduleCheck.NoDates -> PpDocSection(title, stringResource(R.string.project_review_no_dates), PpHighlight.Yellow)
        is ScheduleCheck.Overdue ->
            PpDocSection(title, pluralStringResource(R.plurals.project_review_overdue, check.days.toInt(), dates, check.days), PpHighlight.Red)
        is ScheduleCheck.DueSoon -> if (check.days == 0L) {
            PpDocSection(title, stringResource(R.string.project_review_due_today, dates), PpHighlight.Orange)
        } else {
            PpDocSection(title, pluralStringResource(R.plurals.project_review_due_soon, check.days.toInt(), dates, check.days), PpHighlight.Orange)
        }
        ScheduleCheck.Done -> {
            val completed = project.completedAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
            val text = listOfNotNull(dates.ifEmpty { null }, completed?.let { stringResource(R.string.project_review_completed_on, formatDate(it)) })
                .joinToString(". ")
            PpDocSection(title, text.ifEmpty { stringResource(R.string.project_schedule_done) })
        }
        ScheduleCheck.OnTrack -> PpDocSection(title, dates)
    }
}
