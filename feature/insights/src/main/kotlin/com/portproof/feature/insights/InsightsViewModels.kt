package com.portproof.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.ActivityRepository
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.model.ActivityEntry
import com.portproof.core.model.ActivityInsights
import com.portproof.core.model.Project
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

internal const val INSIGHT_DAYS = 7

/** "Project overview": counts, this week's activity, upcoming deadlines and the latest changes. */
data class OverviewUiState(
    val isLoading: Boolean = true,
    val summary: ProjectSummary = ProjectSummary.Empty,
    val insights: ActivityInsights? = null,
    val deadlines: List<Project> = emptyList(),
    val recentActivity: List<ActivityEntry> = emptyList(),
    val today: LocalDate = LocalDate.MIN,
)

@HiltViewModel
class OverviewViewModel @Inject constructor(
    projectRepository: ProjectRepository,
    activityRepository: ActivityRepository,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)

    val uiState: StateFlow<OverviewUiState> = combine(
        projectRepository.observeSummary(),
        activityRepository.observeActivityInsights(INSIGHT_DAYS),
        projectRepository.observeUpcomingDeadlines(today.plusDays(INSIGHT_DAYS - 1L), limit = 10),
        activityRepository.observeRecentActivity(limit = 4),
    ) { summary, insights, deadlines, activity ->
        OverviewUiState(false, summary, insights, deadlines, activity, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OverviewUiState(today = today))
}

/** Full activity log, newest first. */
data class ActivityLogUiState(val isLoading: Boolean = true, val entries: List<ActivityEntry> = emptyList())

@HiltViewModel
class ActivityLogViewModel @Inject constructor(activityRepository: ActivityRepository) : ViewModel() {
    val uiState: StateFlow<ActivityLogUiState> = activityRepository.observeRecentActivity(limit = 300)
        .map { ActivityLogUiState(isLoading = false, entries = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityLogUiState())
}
