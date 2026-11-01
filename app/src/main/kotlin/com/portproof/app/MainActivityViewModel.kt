package com.portproof.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.UserPreferencesRepository
import com.portproof.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Ready(val themeMode: ThemeMode) : MainActivityUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = preferencesRepository.preferences
        .map<_, MainActivityUiState> { MainActivityUiState.Ready(it.themeMode) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainActivityUiState.Loading)
}
