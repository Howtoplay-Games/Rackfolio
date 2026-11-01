package com.portproof.feature.login.unlock

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.AppLockRepository
import com.portproof.core.data.repository.AppLockSession
import com.portproof.core.data.repository.UserPreferencesRepository
import com.portproof.core.model.PinCheck
import com.portproof.feature.login.PinEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class UnlockUiState(
    val technicianName: String = "",
    val biometricEnabled: Boolean = false,
    /** Set while wrong attempts have imposed a wait. */
    val lockedUntil: Instant? = null,
)

sealed interface UnlockFeedback {
    data class Wrong(val attemptsLeft: Int) : UnlockFeedback
    data object Error : UnlockFeedback
}

/** The login screen: PIN pad, optional fingerprint unlock, wrong-attempt waits. */
@HiltViewModel
class UnlockViewModel @Inject constructor(
    private val lockRepository: AppLockRepository,
    private val session: AppLockSession,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<UnlockUiState> = combine(lockRepository.settings, preferencesRepository.preferences) { lock, prefs ->
        UnlockUiState(prefs.technicianName.trim(), lock.biometricEnabled, lock.lockedUntil)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UnlockUiState())

    var pin by mutableStateOf(PinEntry())
        private set
    var feedback by mutableStateOf<UnlockFeedback?>(null)
        private set
    var isChecking by mutableStateOf(false)
        private set

    fun onDigit(digit: Char) {
        if (isChecking) return
        pin = pin.plus(digit)
        if (pin.digits.length == 1) feedback = null
        if (pin.isComplete) check(pin.digits)
    }

    fun onBackspace() {
        if (!isChecking) pin = pin.backspace()
    }

    /** Called after a successful system biometric prompt. */
    fun onBiometricSuccess() {
        viewModelScope.launch {
            runCatching { lockRepository.clearFailedAttempts() }
            session.unlock()
        }
    }

    private fun check(entered: String) {
        isChecking = true
        viewModelScope.launch {
            try {
                when (val result = lockRepository.checkPin(entered)) {
                    PinCheck.Correct -> session.unlock()
                    is PinCheck.Wrong -> feedback = UnlockFeedback.Wrong(result.attemptsLeft)
                    is PinCheck.LockedOut -> feedback = null
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                feedback = UnlockFeedback.Error
            } finally {
                pin = PinEntry()
                isChecking = false
            }
        }
    }
}
