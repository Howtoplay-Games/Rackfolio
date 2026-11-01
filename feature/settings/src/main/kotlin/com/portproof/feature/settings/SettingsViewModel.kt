package com.portproof.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.PlanRepository
import com.portproof.core.data.repository.ProjectRepository
import com.portproof.core.data.repository.UserPreferencesRepository
import com.portproof.core.model.AppPlan
import com.portproof.core.model.ProfileRules
import com.portproof.core.model.ThemeMode
import com.portproof.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Ready(
        val preferences: UserPreferences,
        val plan: AppPlan = AppPlan.Free(),
        /** Projects counting towards the free plan's limit. */
        val activeProjects: Int = 0,
    ) : SettingsUiState
}

enum class ProfileField(val maxLength: Int) {
    TECHNICIAN_NAME(ProfileRules.TECHNICIAN_NAME_MAX_LENGTH),
    COMPANY_NAME(ProfileRules.COMPANY_NAME_MAX_LENGTH),
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    planRepository: PlanRepository,
    projectRepository: ProjectRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.preferences,
        planRepository.plan,
        projectRepository.observeSummary(),
    ) { preferences, plan, summary -> SettingsUiState.Ready(preferences, plan, summary.active) as SettingsUiState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    /** Validates and stores a profile value. Returns false (and stores nothing) when it is too long. */
    fun updateProfile(field: ProfileField, raw: String): Boolean {
        val value = ProfileRules.sanitize(raw, field.maxLength) ?: return false
        viewModelScope.launch {
            when (field) {
                ProfileField.TECHNICIAN_NAME -> preferencesRepository.setTechnicianName(value)
                ProfileField.COMPANY_NAME -> preferencesRepository.setCompanyName(value)
            }
        }
        return true
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }
}
