package com.portproof.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.data.repository.UserPreferencesRepository
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.core.model.ProjectSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val technicianName: String = "",
    val summary: ProjectSummary = ProjectSummary.Empty,
    /** Most recently updated open projects, shown as feature cards. */
    val activeProjects: List<Project> = emptyList(),
    /** Open projects due within the next 7 days, overdue ones included. */
    val dueThisWeek: Int = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    projectRepository: ProjectRepository,
    preferencesRepository: UserPreferencesRepository,
    clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        preferencesRepository.preferences,
        projectRepository.observeSummary(),
        projectRepository.observeProjects(ProjectListFilter.ACTIVE, ProjectSort.RECENTLY_UPDATED)
            .map { it.take(FEATURED_PROJECTS) },
        projectRepository.observeUpcomingDeadlines(LocalDate.now(clock).plusDays(6), DEADLINE_LIMIT).map { it.size },
    ) { preferences, summary, active, due ->
        DashboardUiState(
            isLoading = false,
            technicianName = preferences.technicianName.trim(),
            summary = summary,
            activeProjects = active,
            dueThisWeek = due,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    companion object {
        const val FEATURED_PROJECTS = 3
        const val DEADLINE_LIMIT = 500
    }
}
