package com.portproof.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.ActivityRepository
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.data.repository.UserPreferencesRepository
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ActivityTrend
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val technicianName: String = "",
    val summary: ProjectSummary = ProjectSummary.Empty,
    val recentProjects: List<Project> = emptyList(),
    val recentActivity: List<ActivityEntry> = emptyList(),
    val trend: ActivityTrend? = null,
) {
    /** First word of the technician name, for the greeting. */
    val firstName: String get() = technicianName.trim().substringBefore(' ')
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    projectRepository: ProjectRepository,
    activityRepository: ActivityRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        preferencesRepository.preferences,
        projectRepository.observeSummary(),
        projectRepository.observeRecentProjects(limit = RECENT_PROJECTS),
        activityRepository.observeRecentActivity(limit = RECENT_ACTIVITY),
        activityRepository.observeActivityTrend(days = TREND_DAYS),
    ) { preferences, summary, projects, activity, trend ->
        DashboardUiState(
            isLoading = false,
            technicianName = preferences.technicianName,
            summary = summary,
            recentProjects = projects,
            recentActivity = activity,
            trend = trend,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    companion object {
        const val RECENT_PROJECTS = 4
        const val RECENT_ACTIVITY = 6
        const val TREND_DAYS = 7
    }
}
