package com.portproof.feature.projects.detail

import com.portproof.core.model.ScheduleState
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** How the schedule line of the project record is flagged. */
sealed interface ScheduleCheck {
    data object NoDates : ScheduleCheck
    data class Overdue(val days: Long) : ScheduleCheck
    data class DueSoon(val days: Long) : ScheduleCheck
    data object OnTrack : ScheduleCheck
    data object Done : ScheduleCheck
}

/**
 * Rule-based checks of a project record, shown as highlighted lines on the detail screen. These
 * are simple checks of what has been entered, not an assessment of the installation itself.
 */
data class ProjectReview(
    val missingCustomer: Boolean,
    val missingSite: Boolean,
    val schedule: ScheduleCheck,
    val onHold: Boolean,
    val archived: Boolean,
) {
    val issueCount: Int
        get() = listOf(
            missingCustomer,
            missingSite,
            schedule is ScheduleCheck.NoDates || schedule is ScheduleCheck.Overdue || schedule is ScheduleCheck.DueSoon,
            onHold,
            archived,
        ).count { it }
}

const val DUE_SOON_DAYS = 7L

fun reviewOf(project: Project, today: LocalDate): ProjectReview {
    val end = project.endDate
    val schedule = when {
        project.status == ProjectStatus.COMPLETED -> ScheduleCheck.Done
        project.startDate == null && end == null -> ScheduleCheck.NoDates
        end != null && end.isBefore(today) -> ScheduleCheck.Overdue(ChronoUnit.DAYS.between(end, today))
        end != null && ChronoUnit.DAYS.between(today, end) < DUE_SOON_DAYS -> ScheduleCheck.DueSoon(ChronoUnit.DAYS.between(today, end))
        else -> ScheduleCheck.OnTrack
    }
    return ProjectReview(
        missingCustomer = project.customerName == null,
        missingSite = project.siteName == null && project.siteAddress == null,
        schedule = schedule,
        onHold = project.status == ProjectStatus.ON_HOLD,
        archived = project.isArchived,
    )
}

/** Filled segments (of [total]) for the review card: schedule progress if known, else the status. */
fun progressSegments(project: Project, schedule: ScheduleState?, total: Int = 5): Int = when {
    project.status == ProjectStatus.COMPLETED -> total
    schedule != null -> (schedule.progress * total).toInt().coerceIn(0, total)
    project.status == ProjectStatus.PLANNED -> 1
    else -> total / 2 + 1
}
