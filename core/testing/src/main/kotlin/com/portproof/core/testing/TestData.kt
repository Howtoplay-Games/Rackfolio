package com.portproof.core.testing

import com.portproof.core.model.Project
import com.portproof.core.model.ProjectDraft
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectInput
import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.ProjectValidationResult
import com.portproof.core.model.ProjectValidator
import java.time.Instant

val TestInstant: Instant = Instant.parse("2026-11-01T10:00:00Z")

fun testProject(
    id: String = "project-1",
    name: String = "Head office CCTV upgrade",
    status: ProjectStatus = ProjectStatus.IN_PROGRESS,
    customerName: String? = "Northwind Logistics",
    siteName: String? = "Head office",
    archived: Boolean = false,
    updatedAt: Instant = TestInstant,
) = Project(
    id = ProjectId(id),
    name = name,
    reference = "NW-2026-014",
    customerName = customerName,
    siteName = siteName,
    siteAddress = "12 Harbour Road\nBristol",
    description = null,
    status = status,
    startDate = null,
    endDate = null,
    createdAt = TestInstant,
    updatedAt = updatedAt,
    completedAt = if (status == ProjectStatus.COMPLETED) TestInstant else null,
    archivedAt = if (archived) TestInstant else null,
)

/** Drafts can only come from the validator; this helper fails fast on invalid input. */
fun ProjectInput.toDraft(): ProjectDraft =
    when (val result = ProjectValidator.validate(this)) {
        is ProjectValidationResult.Valid -> result.draft
        is ProjectValidationResult.Invalid -> error("Invalid test input: ${result.errors}")
    }
