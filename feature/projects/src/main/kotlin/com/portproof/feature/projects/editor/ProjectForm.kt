package com.portproof.feature.projects.editor

import androidx.lifecycle.SavedStateHandle
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectInput
import com.portproof.core.model.ProjectStatus
import java.time.LocalDate

/** Editable form values exactly as typed. Validation happens on save via ProjectValidator. */
data class ProjectForm(
    val name: String = "",
    val reference: String = "",
    val customerName: String = "",
    val siteName: String = "",
    val siteAddress: String = "",
    val description: String = "",
    val status: ProjectStatus = ProjectStatus.PLANNED,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
) {
    fun toInput() = ProjectInput(name, reference, customerName, siteName, siteAddress, description, status, startDate, endDate)

    companion object {
        fun from(project: Project) = ProjectForm(
            name = project.name,
            reference = project.reference.orEmpty(),
            customerName = project.customerName.orEmpty(),
            siteName = project.siteName.orEmpty(),
            siteAddress = project.siteAddress.orEmpty(),
            description = project.description.orEmpty(),
            status = project.status,
            startDate = project.startDate,
            endDate = project.endDate,
        )
    }
}

/** Persists the form across process death using only Bundle-safe primitives. */
internal object ProjectFormSaver {
    private const val PREFIX = "editor.form."
    private const val MARKER = PREFIX + "saved"

    fun save(handle: SavedStateHandle, form: ProjectForm) {
        handle[MARKER] = true
        handle[PREFIX + "name"] = form.name
        handle[PREFIX + "reference"] = form.reference
        handle[PREFIX + "customer"] = form.customerName
        handle[PREFIX + "site"] = form.siteName
        handle[PREFIX + "address"] = form.siteAddress
        handle[PREFIX + "description"] = form.description
        handle[PREFIX + "status"] = form.status.code
        handle[PREFIX + "start"] = form.startDate?.toEpochDay()
        handle[PREFIX + "end"] = form.endDate?.toEpochDay()
    }

    fun restore(handle: SavedStateHandle): ProjectForm? {
        if (handle.get<Boolean>(MARKER) != true) return null
        return ProjectForm(
            name = handle.get<String>(PREFIX + "name").orEmpty(),
            reference = handle.get<String>(PREFIX + "reference").orEmpty(),
            customerName = handle.get<String>(PREFIX + "customer").orEmpty(),
            siteName = handle.get<String>(PREFIX + "site").orEmpty(),
            siteAddress = handle.get<String>(PREFIX + "address").orEmpty(),
            description = handle.get<String>(PREFIX + "description").orEmpty(),
            status = ProjectStatus.fromCode(handle.get<String>(PREFIX + "status")) ?: ProjectStatus.PLANNED,
            startDate = handle.get<Long>(PREFIX + "start")?.let(LocalDate::ofEpochDay),
            endDate = handle.get<Long>(PREFIX + "end")?.let(LocalDate::ofEpochDay),
        )
    }
}
