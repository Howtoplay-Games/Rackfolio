package com.portproof.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.billing.SubscriptionRepository
import com.portproof.core.data.repository.AppLockRepository
import com.portproof.core.data.repository.AppLockSession
import com.portproof.core.data.repository.UserPreferencesRepository
import com.portproof.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** What the app shows: the first-run login setup, the login (unlock) screen, or the app itself. */
enum class LoginGate { Setup, Locked, Unlocked }

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Ready(val themeMode: ThemeMode, val gate: LoginGate) : MainActivityUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository,
    lockRepository: AppLockRepository,
    private val session: AppLockSession,
    subscriptions: SubscriptionRepository,
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = combine(
        preferencesRepository.preferences,
        lockRepository.settings,
        session.unlocked,
    ) { preferences, lock, unlocked ->
        val gate = when {
            !lock.pinSet -> LoginGate.Setup
            !unlocked -> LoginGate.Locked
            else -> LoginGate.Unlocked
        }
        MainActivityUiState.Ready(preferences.themeMode, gate) as MainActivityUiState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainActivityUiState.Loading)

    init {
        // Re-check the subscription once per launch; the cached plan applies until Google Play answers.
        viewModelScope.launch { subscriptions.refresh() }
    }

    fun onAppBackgrounded() = session.onAppBackgrounded()

    fun onAppForegrounded() = session.onAppForegrounded()
}
