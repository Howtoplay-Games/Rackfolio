package com.portproof.core.model

import java.time.Instant
import java.time.LocalDate

/** What kind of record an activity entry refers to. Later phases add equipment, cables, tests... */
enum class ActivityEntityType(val code: String) {
    PROJECT("PROJECT"),
    ;

    companion object {
        fun fromCode(code: String?): ActivityEntityType? = entries.firstOrNull { it.code == code }
    }
}

enum class ActivityAction(val code: String) {
    CREATED("CREATED"),
    UPDATED("UPDATED"),
    STATUS_CHANGED("STATUS_CHANGED"),
    ARCHIVED("ARCHIVED"),
    RESTORED("RESTORED"),
    ;

    companion object {
        fun fromCode(code: String?): ActivityAction? = entries.firstOrNull { it.code == code }
    }
}

/**
 * One entry of the local activity log. Entries never contain customer details or serial
 * numbers: [label] is a display-name snapshot and [detail] a machine-readable code
 * (for [ActivityAction.STATUS_CHANGED], the new [ProjectStatus.code]).
 */
data class ActivityEntry(
    val id: Long,
    val projectId: ProjectId?,
    val projectName: String?,
    val entityType: ActivityEntityType,
    val entityId: String?,
    val action: ActivityAction,
    val label: String?,
    val detail: String?,
    val occurredAt: Instant,
) {
    val newStatus: ProjectStatus?
        get() = if (action == ActivityAction.STATUS_CHANGED) ProjectStatus.fromCode(detail) else null
}

data class DailyActivity(val date: LocalDate, val count: Int)

/** Activity per day for the current period plus the total of the period before it. */
data class ActivityTrend(
    val days: List<DailyActivity>,
    val previousPeriodTotal: Int,
) {
    val total: Int get() = days.sumOf { it.count }

    /** Relative change versus the previous period, or null when there is nothing to compare. */
    val changeRatio: Float?
        get() = if (previousPeriodTotal == 0) null else (total - previousPeriodTotal).toFloat() / previousPeriodTotal
}
