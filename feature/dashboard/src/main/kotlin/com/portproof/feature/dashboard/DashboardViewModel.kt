package com.portproof.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.data.repository.UserPreferencesRepository
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
    val technicianName: String = "",
    val summary: ProjectSummary = ProjectSummary.Empty,
    val today: LocalDate = LocalDate.MIN,
) {
    /** First word of the technician name, shown on the greeting's second line. */
    val firstName: String get() = technicianName.substringBefore(' ')
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    projectRepository: ProjectRepository,
    preferencesRepository: UserPreferencesRepository,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)

    val uiState: StateFlow<DashboardUiState> = combine(
        preferencesRepository.preferences,
        projectRepository.observeSummary(),
    ) { preferences, summary ->
        DashboardUiState(
            isLoading = false,
            technicianName = preferences.technicianName.trim(),
            summary = summary,
            today = today,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState(today = today))
}
