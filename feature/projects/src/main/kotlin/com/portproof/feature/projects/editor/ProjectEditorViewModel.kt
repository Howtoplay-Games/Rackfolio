package com.portproof.feature.projects.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.PlanRepository
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.model.ActiveProjectLimitException
import com.portproof.core.model.FieldError
import com.portproof.core.model.ProjectField
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
import com.portproof.core.model.ProjectValidationResult
import com.portproof.core.model.ProjectValidator
import com.portproof.core.model.activeProjectLimit
import com.portproof.feature.projects.navigation.ProjectArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class EditorLoadState { Loading, Ready, NotFound }

enum class EditorMessage { FixFields, SaveFailed, LimitReached }

/**
 * Create/edit form. State lives in Compose snapshot state so text fields update synchronously,
 * and is mirrored into [SavedStateHandle] so nothing typed on site is lost to process death.
 */
@HiltViewModel
class ProjectEditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    planRepository: PlanRepository,
) : ViewModel() {

    private val editingId: ProjectId? = savedStateHandle.get<String>(ProjectArgs.PROJECT_ID)?.let(::ProjectId)
    val isNew: Boolean = editingId == null

    var form by mutableStateOf(ProjectFormSaver.restore(savedStateHandle) ?: ProjectForm())
        private set
    private var original by mutableStateOf(if (isNew) ProjectForm() else null)

    var loadState by mutableStateOf(if (isNew) EditorLoadState.Ready else EditorLoadState.Loading)
        private set
    var errors by mutableStateOf<Map<ProjectField, FieldError>>(emptyMap())
        private set
    var isSaving by mutableStateOf(false)
        private set
    var message by mutableStateOf<EditorMessage?>(null)
        private set

    /** Set after a successful save; the screen navigates and then calls [onSaveHandled]. */
    var savedProjectId by mutableStateOf<ProjectId?>(null)
        private set

    val hasUnsavedChanges: Boolean get() = original != null && form != original

    /** The free plan's limit while every active slot is used; null on Pro or below the limit. */
    private var fullActiveLimit by mutableStateOf<Int?>(null)

    /**
     * Non-null (the limit) when saving this form would add an active project the plan has no room
     * for: a new open project, or a completed one being reopened.
     */
    val limitNotice: Int?
        get() {
            val limit = fullActiveLimit ?: return null
            val addsActive = form.status.isOpen && (isNew || original?.status == ProjectStatus.COMPLETED)
            return limit.takeIf { addsActive }
        }

    init {
        if (editingId != null) loadExisting(editingId)
        viewModelScope.launch {
            combine(planRepository.plan, projectRepository.observeSummary()) { plan, summary ->
                plan.activeProjectLimit?.takeIf { summary.active >= it }
            }.collect { fullActiveLimit = it }
        }
    }

    private fun loadExisting(id: ProjectId) {
        viewModelScope.launch {
            val project = try {
                projectRepository.getProject(id)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            if (project == null) {
                loadState = EditorLoadState.NotFound
                return@launch
            }
            val loaded = ProjectForm.from(project)
            original = loaded
            // After process death the restored (possibly edited) form wins over the stored record.
            if (ProjectFormSaver.restore(savedStateHandle) == null) form = loaded
            loadState = EditorLoadState.Ready
        }
    }

    fun onNameChange(value: String) = update(ProjectField.NAME) { copy(name = value) }
    fun onReferenceChange(value: String) = update(ProjectField.REFERENCE) { copy(reference = value) }
    fun onCustomerNameChange(value: String) = update(ProjectField.CUSTOMER_NAME) { copy(customerName = value) }
    fun onSiteNameChange(value: String) = update(ProjectField.SITE_NAME) { copy(siteName = value) }
    fun onSiteAddressChange(value: String) = update(ProjectField.SITE_ADDRESS) { copy(siteAddress = value) }
    fun onDescriptionChange(value: String) = update(ProjectField.DESCRIPTION) { copy(description = value) }
    fun onStatusChange(value: ProjectStatus) = update(null) { copy(status = value) }
    fun onStartDateChange(value: LocalDate?) = update(ProjectField.END_DATE) { copy(startDate = value) }
    fun onEndDateChange(value: LocalDate?) = update(ProjectField.END_DATE) { copy(endDate = value) }

    private fun update(field: ProjectField?, transform: ProjectForm.() -> ProjectForm) {
        form = form.transform()
        ProjectFormSaver.save(savedStateHandle, form)
        if (field != null && field in errors) errors = errors - field
    }

    fun save() {
        if (isSaving || loadState != EditorLoadState.Ready) return
        val draft = when (val result = ProjectValidator.validate(form.toInput())) {
            is ProjectValidationResult.Invalid -> {
                errors = result.errors
                message = EditorMessage.FixFields
                return
            }
            is ProjectValidationResult.Valid -> result.draft
        }
        errors = emptyMap()
        isSaving = true
        viewModelScope.launch {
            try {
                val savedId = if (editingId == null) {
                    projectRepository.createProject(draft)
                } else {
                    editingId.takeIf { projectRepository.updateProject(it, draft) }
                }
                if (savedId == null) {
                    loadState = EditorLoadState.NotFound
                } else {
                    original = form
                    savedProjectId = savedId
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: ActiveProjectLimitException) {
                message = EditorMessage.LimitReached
            } catch (_: Exception) {
                message = EditorMessage.SaveFailed
            } finally {
                isSaving = false
            }
        }
    }

    fun onSaveHandled() {
        savedProjectId = null
    }

    fun onMessageShown() {
        message = null
    }
}
