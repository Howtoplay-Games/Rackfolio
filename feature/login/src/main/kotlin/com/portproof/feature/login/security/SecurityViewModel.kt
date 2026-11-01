package com.portproof.feature.login.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.AppLockRepository
import com.portproof.core.data.repository.AppLockSession
import com.portproof.core.model.AppLockSettings
import com.portproof.core.model.PinCheck
import com.portproof.core.model.PinError
import com.portproof.core.model.PinRules
import com.portproof.feature.login.PinEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Steps of the change-PIN flow; null when it is not open. */
enum class ChangePinStep { Current, New, Confirm }

enum class SecurityMessage { WrongPin, LockedOut, TooSimple, Mismatch, PinChanged, Error }

/** "Login & security": change the PIN, turn fingerprint unlock on or off, lock now. */
@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val lockRepository: AppLockRepository,
    private val session: AppLockSession,
) : ViewModel() {

    val settings: StateFlow<AppLockSettings> =
        lockRepository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLockSettings.None)

    var changeStep by mutableStateOf<ChangePinStep?>(null)
        private set
    var pin by mutableStateOf(PinEntry())
        private set
    var message by mutableStateOf<SecurityMessage?>(null)
        private set
    var isBusy by mutableStateOf(false)
        private set

    private var newPin: String? = null

    fun startChangePin() {
        newPin = null
        pin = PinEntry()
        changeStep = ChangePinStep.Current
    }

    fun cancelChangePin() {
        newPin = null
        pin = PinEntry()
        changeStep = null
    }

    fun onDigit(digit: Char) {
        val step = changeStep ?: return
        if (isBusy) return
        pin = pin.plus(digit)
        if (!pin.isComplete) return
        val entered = pin.digits
        pin = PinEntry()
        when (step) {
            ChangePinStep.Current -> verifyCurrent(entered)
            ChangePinStep.New -> when (PinRules.validate(entered)) {
                null -> {
                    newPin = entered
                    changeStep = ChangePinStep.Confirm
                }
                PinError.TOO_SIMPLE, PinError.FORMAT -> message = SecurityMessage.TooSimple
            }
            ChangePinStep.Confirm -> if (entered == newPin) save(entered) else {
                newPin = null
                changeStep = ChangePinStep.New
                message = SecurityMessage.Mismatch
            }
        }
    }

    fun onBackspace() {
        pin = pin.backspace()
    }

    /** Call with true only after the system biometric prompt succeeded. */
    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { runCatching { lockRepository.setBiometricEnabled(enabled) }.onFailure { message = SecurityMessage.Error } }
    }

    fun lockNow() = session.lock()

    fun onMessageShown() {
        message = null
    }

    private fun verifyCurrent(entered: String) = busy {
        when (lockRepository.checkPin(entered)) {
            PinCheck.Correct -> changeStep = ChangePinStep.New
            is PinCheck.Wrong -> message = SecurityMessage.WrongPin
            is PinCheck.LockedOut -> {
                message = SecurityMessage.LockedOut
                changeStep = null
            }
        }
    }

    private fun save(entered: String) = busy {
        lockRepository.setPin(entered)
        newPin = null
        changeStep = null
        message = SecurityMessage.PinChanged
    }

    private fun busy(block: suspend () -> Unit) {
        isBusy = true
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                message = SecurityMessage.Error
            } finally {
                isBusy = false
            }
        }
    }
}
