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

/** "Metrics Summary" tab: daily activity chart plus the project counts grid. */
data class MetricsUiState(
    val isLoading: Boolean = true,
    val summary: ProjectSummary = ProjectSummary.Empty,
    val insights: ActivityInsights? = null,
)

@HiltViewModel
class MetricsViewModel @Inject constructor(
    projectRepository: ProjectRepository,
    activityRepository: ActivityRepository,
) : ViewModel() {
    val uiState: StateFlow<MetricsUiState> = combine(
        projectRepository.observeSummary(),
        activityRepository.observeActivityInsights(INSIGHT_DAYS),
    ) { summary, insights -> MetricsUiState(false, summary, insights) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MetricsUiState())
}

/** "Insight" detail screen: activity by hour, completion, and upcoming completion dates. */
data class InsightUiState(
    val isLoading: Boolean = true,
    val summary: ProjectSummary = ProjectSummary.Empty,
    val insights: ActivityInsights? = null,
    val deadlines: List<Project> = emptyList(),
    val today: LocalDate = LocalDate.MIN,
)

@HiltViewModel
class InsightViewModel @Inject constructor(
    projectRepository: ProjectRepository,
    activityRepository: ActivityRepository,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)

    val uiState: StateFlow<InsightUiState> = combine(
        projectRepository.observeSummary(),
        activityRepository.observeActivityInsights(INSIGHT_DAYS),
        projectRepository.observeUpcomingDeadlines(today.plusDays(INSIGHT_DAYS - 1L), limit = 20),
    ) { summary, insights, deadlines -> InsightUiState(false, summary, insights, deadlines, today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightUiState())
}

/** Full activity log behind the dashboard bell, newest first. */
data class ActivityLogUiState(val isLoading: Boolean = true, val entries: List<ActivityEntry> = emptyList())

@HiltViewModel
class ActivityLogViewModel @Inject constructor(activityRepository: ActivityRepository) : ViewModel() {
    val uiState: StateFlow<ActivityLogUiState> = activityRepository.observeRecentActivity(limit = 300)
        .map { ActivityLogUiState(isLoading = false, entries = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityLogUiState())
}
