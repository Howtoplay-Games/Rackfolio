package com.portproof.core.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/** Stable identifier of a project. Also used in QR payloads, so it never contains personal data. */
@JvmInline
value class ProjectId(val value: String) {
    companion object {
        fun random(): ProjectId = ProjectId(UUID.randomUUID().toString())
    }
}

/**
 * Lifecycle of an installation project. [code] is the persisted value and must never change;
 * enum constants may be renamed freely.
 */
enum class ProjectStatus(val code: String) {
    PLANNED("PLANNED"),
    IN_PROGRESS("IN_PROGRESS"),
    ON_HOLD("ON_HOLD"),
    COMPLETED("COMPLETED"),
    ;

    /** Open projects count as "active" on the dashboard. */
    val isOpen: Boolean get() = this != COMPLETED

    companion object {
        fun fromCode(code: String?): ProjectStatus? = entries.firstOrNull { it.code == code }
    }
}

data class Project(
    val id: ProjectId,
    val name: String,
    val reference: String?,
    val customerName: String?,
    val siteName: String?,
    val siteAddress: String?,
    val description: String?,
    val status: ProjectStatus,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant?,
    val archivedAt: Instant?,
) {
    val isArchived: Boolean get() = archivedAt != null
}
