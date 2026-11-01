package com.portproof.core.data.mapper

import com.portproof.core.database.model.ActivityWithProjectRow
import com.portproof.core.database.model.ProjectCountsRow
import com.portproof.core.database.model.ProjectEntity
import com.portproof.core.model.ActivityAction
import com.portproof.core.model.ActivityEntityType
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.ProjectSummary
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate

/** Unknown status codes (e.g. from a corrupted or newer backup) degrade to PLANNED instead of crashing. */
internal fun ProjectEntity.asExternalModel() = Project(
    id = ProjectId(id),
    name = name,
    reference = reference,
    customerName = customerName,
    siteName = siteName,
    siteAddress = siteAddress,
    description = description,
    status = ProjectStatus.fromCode(status) ?: ProjectStatus.PLANNED,
    startDate = startDateEpochDay?.toLocalDateOrNull(),
    endDate = endDateEpochDay?.toLocalDateOrNull(),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    completedAt = completedAt?.let(Instant::ofEpochMilli),
    archivedAt = archivedAt?.let(Instant::ofEpochMilli),
)

internal fun ProjectCountsRow.asExternalModel() = ProjectSummary(
    total = total,
    active = active,
    completed = completed,
    archived = archived,
    planned = planned,
    inProgress = inProgress,
    onHold = onHold,
    createdThisMonth = createdThisMonth,
    completedThisMonth = completedThisMonth,
)

/** Returns null for rows with unknown codes so a single bad row cannot break the activity feed. */
internal fun ActivityWithProjectRow.asExternalModelOrNull(): ActivityEntry? {
    val type = ActivityEntityType.fromCode(activity.entityType) ?: return null
    val action = ActivityAction.fromCode(activity.action) ?: return null
    return ActivityEntry(
        id = activity.id,
        projectId = activity.projectId?.let(::ProjectId),
        projectName = projectName,
        entityType = type,
        entityId = activity.entityId,
        action = action,
        label = activity.label,
        detail = activity.detail,
        occurredAt = Instant.ofEpochMilli(activity.occurredAt),
    )
}

private fun Long.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.ofEpochDay(this)
} catch (_: DateTimeException) {
    null
}
