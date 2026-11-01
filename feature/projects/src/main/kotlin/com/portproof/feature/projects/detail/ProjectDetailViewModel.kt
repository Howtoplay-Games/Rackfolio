package com.portproof.feature.projects.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.ActivityRepository
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectStatus
import com.portproof.feature.projects.navigation.ProjectArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

sealed interface ProjectDetailUiState {
    data object Loading : ProjectDetailUiState
    data object NotFound : ProjectDetailUiState
    data class Success(
        val project: Project,
        val activity: List<ActivityEntry>,
        val schedule: ScheduleState?,
        val review: ProjectReview,
        val today: LocalDate,
    ) : ProjectDetailUiState
}

enum class ProjectDetailMessage { Archived, Restored, Error }

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    activityRepository: ActivityRepository,
    clock: Clock,
) : ViewModel() {

    val projectId = ProjectId(checkNotNull(savedStateHandle.get<String>(ProjectArgs.PROJECT_ID)))

    val uiState: StateFlow<ProjectDetailUiState> = combine(
        projectRepository.observeProject(projectId),
        activityRepository.observeProjectActivity(projectId, limit = ACTIVITY_LIMIT),
    ) { project, activity ->
        if (project == null) {
            ProjectDetailUiState.NotFound
        } else {
            val today = LocalDate.now(clock)
            ProjectDetailUiState.Success(project, activity, scheduleStateOf(project, today), reviewOf(project, today), today)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectDetailUiState.Loading)

    private val _message = MutableStateFlow<ProjectDetailMessage?>(null)
    val message: StateFlow<ProjectDetailMessage?> = _message.asStateFlow()

    /** Becomes true once the project has been deleted; the screen then navigates away. */
    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun setStatus(status: ProjectStatus) = mutate { projectRepository.setStatus(projectId, status) }

    fun archive() = mutate(onSuccess = ProjectDetailMessage.Archived) { projectRepository.archiveProject(projectId) }

    fun restore() = mutate(onSuccess = ProjectDetailMessage.Restored) { projectRepository.restoreProject(projectId) }

    fun delete() = mutate {
        projectRepository.deleteProject(projectId).also { if (it) _deleted.value = true }
    }

    fun onMessageShown() {
        _message.value = null
    }

    /** Runs a write; storage failures become a user message instead of a crash. */
    private fun mutate(onSuccess: ProjectDetailMessage? = null, block: suspend () -> Boolean) {
        viewModelScope.launch {
            try {
                if (block() && onSuccess != null) _message.value = onSuccess
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _message.value = ProjectDetailMessage.Error
            }
        }
    }

    private companion object {
        const val ACTIVITY_LIMIT = 20
    }
}
