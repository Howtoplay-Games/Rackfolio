package com.portproof.core.model

/** Project list filters. [code] is used in navigation arguments and SQL, so it must stay stable. */
enum class ProjectListFilter(val code: String) {
    /** Every project that is not archived. */
    ALL("ALL"),

    /** Not archived and not completed. */
    ACTIVE("ACTIVE"),

    /** Not archived and completed. */
    COMPLETED("COMPLETED"),

    ARCHIVED("ARCHIVED"),
    ;

    companion object {
        fun fromCode(code: String?): ProjectListFilter? = entries.firstOrNull { it.code == code }
    }
}

enum class ProjectSort(val code: String) {
    RECENTLY_UPDATED("RECENTLY_UPDATED"),
    NAME("NAME"),
    START_DATE("START_DATE"),
    ;

    companion object {
        fun fromCode(code: String?): ProjectSort? = entries.firstOrNull { it.code == code }
    }
}

/**
 * Aggregated project counts for the dashboard. Totals exclude archived projects;
 * [total] == [active] + [completed] and [active] == [planned] + [inProgress] + [onHold].
 */
data class ProjectSummary(
    val total: Int,
    val active: Int,
    val completed: Int,
    val archived: Int,
    val planned: Int,
    val inProgress: Int,
    val onHold: Int,
    val createdThisMonth: Int,
    val completedThisMonth: Int,
) {
    val completionRatio: Float get() = if (total == 0) 0f else completed.toFloat() / total

    /** True when the user has never created a project (including archived ones). */
    val isEmpty: Boolean get() = total == 0 && archived == 0

    companion object {
        val Empty = ProjectSummary(0, 0, 0, 0, 0, 0, 0, 0, 0)
    }
}
