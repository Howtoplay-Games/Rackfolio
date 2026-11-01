package com.portproof.core.model

import java.time.LocalDate

/** Raw, unvalidated project form input exactly as typed by the user. */
data class ProjectInput(
    val name: String = "",
    val reference: String = "",
    val customerName: String = "",
    val siteName: String = "",
    val siteAddress: String = "",
    val description: String = "",
    val status: ProjectStatus = ProjectStatus.PLANNED,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)

/**
 * Validated and normalised project data. It can only be created by [ProjectValidator], so every
 * value that reaches the database has passed validation. Optional blank values are `null`.
 */
@ConsistentCopyVisibility
data class ProjectDraft internal constructor(
    val name: String,
    val reference: String?,
    val customerName: String?,
    val siteName: String?,
    val siteAddress: String?,
    val description: String?,
    val status: ProjectStatus,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)

enum class ProjectField { NAME, REFERENCE, CUSTOMER_NAME, SITE_NAME, SITE_ADDRESS, DESCRIPTION, END_DATE }

sealed interface FieldError {
    data object Required : FieldError
    data class TooLong(val maxLength: Int) : FieldError
    data object EndBeforeStart : FieldError
}

sealed interface ProjectValidationResult {
    data class Valid(val draft: ProjectDraft) : ProjectValidationResult
    data class Invalid(val errors: Map<ProjectField, FieldError>) : ProjectValidationResult
}

object ProjectRules {
    const val NAME_MAX_LENGTH = 120
    const val REFERENCE_MAX_LENGTH = 64
    const val CUSTOMER_NAME_MAX_LENGTH = 120
    const val SITE_NAME_MAX_LENGTH = 120
    const val SITE_ADDRESS_MAX_LENGTH = 500
    const val DESCRIPTION_MAX_LENGTH = 4000
}
