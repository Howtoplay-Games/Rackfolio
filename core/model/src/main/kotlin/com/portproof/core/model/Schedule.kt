package com.portproof.core.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Where a project stands against its planned dates. Requires both dates to be set. */
sealed interface ScheduleState {
    val progress: Float

    data class Upcoming(val daysUntilStart: Long) : ScheduleState {
        override val progress = 0f
    }

    data class Running(val day: Long, val totalDays: Long) : ScheduleState {
        override val progress = (day.toFloat() / totalDays).coerceIn(0f, 1f)
    }

    data class Overdue(val daysOver: Long) : ScheduleState {
        override val progress = 1f
    }

    data object Done : ScheduleState {
        override val progress = 1f
    }
}

fun scheduleStateOf(project: Project, today: LocalDate): ScheduleState? {
    val start = project.startDate ?: return null
    val end = project.endDate ?: return null
    return when {
        project.status == ProjectStatus.COMPLETED -> ScheduleState.Done
        today.isBefore(start) -> ScheduleState.Upcoming(ChronoUnit.DAYS.between(today, start))
        today.isAfter(end) -> ScheduleState.Overdue(ChronoUnit.DAYS.between(end, today))
        else -> ScheduleState.Running(
            day = ChronoUnit.DAYS.between(start, today) + 1,
            totalDays = ChronoUnit.DAYS.between(start, end) + 1,
        )
    }
}
