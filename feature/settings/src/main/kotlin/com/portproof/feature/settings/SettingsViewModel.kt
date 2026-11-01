package com.portproof.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.UserPreferencesRepository
import com.portproof.core.model.ProfileRules
import com.portproof.core.model.ThemeMode
import com.portproof.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Ready(val preferences: UserPreferences) : SettingsUiState
}

enum class ProfileField(val maxLength: Int) {
    TECHNICIAN_NAME(ProfileRules.TECHNICIAN_NAME_MAX_LENGTH),
    COMPANY_NAME(ProfileRules.COMPANY_NAME_MAX_LENGTH),
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = preferencesRepository.preferences
        .map<UserPreferences, SettingsUiState> { SettingsUiState.Ready(it) }
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
