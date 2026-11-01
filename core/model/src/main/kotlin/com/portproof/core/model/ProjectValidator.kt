package com.portproof.core.model

import com.portproof.core.model.ProjectRules.CUSTOMER_NAME_MAX_LENGTH
import com.portproof.core.model.ProjectRules.DESCRIPTION_MAX_LENGTH
import com.portproof.core.model.ProjectRules.NAME_MAX_LENGTH
import com.portproof.core.model.ProjectRules.REFERENCE_MAX_LENGTH
import com.portproof.core.model.ProjectRules.SITE_ADDRESS_MAX_LENGTH
import com.portproof.core.model.ProjectRules.SITE_NAME_MAX_LENGTH

object ProjectValidator {

    fun validate(input: ProjectInput): ProjectValidationResult {
        val errors = linkedMapOf<ProjectField, FieldError>()

        val name = TextSanitizer.singleLine(input.name)
        if (name.isEmpty()) errors[ProjectField.NAME] = FieldError.Required
        checkLength(ProjectField.NAME, name, NAME_MAX_LENGTH, errors)

        val reference = TextSanitizer.singleLine(input.reference)
        checkLength(ProjectField.REFERENCE, reference, REFERENCE_MAX_LENGTH, errors)
        val customerName = TextSanitizer.singleLine(input.customerName)
        checkLength(ProjectField.CUSTOMER_NAME, customerName, CUSTOMER_NAME_MAX_LENGTH, errors)
        val siteName = TextSanitizer.singleLine(input.siteName)
        checkLength(ProjectField.SITE_NAME, siteName, SITE_NAME_MAX_LENGTH, errors)
        val siteAddress = TextSanitizer.multiLine(input.siteAddress)
        checkLength(ProjectField.SITE_ADDRESS, siteAddress, SITE_ADDRESS_MAX_LENGTH, errors)
        val description = TextSanitizer.multiLine(input.description)
        checkLength(ProjectField.DESCRIPTION, description, DESCRIPTION_MAX_LENGTH, errors)

        val start = input.startDate
        val end = input.endDate
        if (start != null && end != null && end.isBefore(start)) {
            errors[ProjectField.END_DATE] = FieldError.EndBeforeStart
        }

        if (errors.isNotEmpty()) return ProjectValidationResult.Invalid(errors)
        return ProjectValidationResult.Valid(
            ProjectDraft(
                name = name,
                reference = reference.ifEmpty { null },
                customerName = customerName.ifEmpty { null },
                siteName = siteName.ifEmpty { null },
                siteAddress = siteAddress.ifEmpty { null },
                description = description.ifEmpty { null },
                status = input.status,
                startDate = start,
                endDate = end,
            ),
        )
    }

    private fun checkLength(field: ProjectField, value: String, max: Int, errors: MutableMap<ProjectField, FieldError>) {
        if (field !in errors && TextSanitizer.length(value) > max) errors[field] = FieldError.TooLong(max)
    }
}
