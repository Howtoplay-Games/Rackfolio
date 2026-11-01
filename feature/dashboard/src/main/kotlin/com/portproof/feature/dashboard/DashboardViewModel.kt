package com.portproof.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: ProjectSummary = ProjectSummary.Empty,
    /** The most recently updated projects, shown as cards under the gauge. */
    val recentProjects: List<Project> = emptyList(),
    val today: LocalDate = LocalDate.MIN,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    projectRepository: ProjectRepository,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)

    val uiState: StateFlow<DashboardUiState> = combine(
        projectRepository.observeSummary(),
        projectRepository.observeRecentProjects(RECENT_LIMIT),
    ) { summary, recent ->
        DashboardUiState(isLoading = false, summary = summary, recentProjects = recent, today = today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState(today = today))

    companion object {
        const val RECENT_LIMIT = 3
    }
}
