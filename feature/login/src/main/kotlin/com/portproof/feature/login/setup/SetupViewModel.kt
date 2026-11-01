package com.portproof.feature.login.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portproof.core.data.repository.AppLockRepository
import com.portproof.core.data.repository.AppLockSession
import com.portproof.core.data.repository.UserPreferencesRepository
import com.portproof.core.model.PinError
import com.portproof.core.model.PinRules
import com.portproof.core.model.ProfileRules
import com.portproof.feature.login.PinEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SetupStep { Welcome, Profile, CreatePin, ConfirmPin, Biometric }

enum class SetupError { NameRequired, NameTooLong, CompanyTooLong, PinTooSimple, PinMismatch, SaveFailed }

/**
 * First-run login setup: welcome pages, name, a 6-digit PIN typed twice, then optional fingerprint
 * unlock. Nothing is stored until the last step, so quitting half way starts again cleanly. The PIN
 * itself is never written to the saved state.
 */
@HiltViewModel
class SetupViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val lockRepository: AppLockRepository,
    private val session: AppLockSession,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    var step by mutableStateOf(SetupStep.valueOf(savedStateHandle[KEY_STEP] ?: SetupStep.Welcome.name).coerceBeforePin())
        private set
    var name by mutableStateOf(savedStateHandle.get<String>(KEY_NAME).orEmpty())
        private set
    var company by mutableStateOf(savedStateHandle.get<String>(KEY_COMPANY).orEmpty())
        private set
    var pin by mutableStateOf(PinEntry())
        private set
    var error by mutableStateOf<SetupError?>(null)
        private set
    var isSaving by mutableStateOf(false)
        private set

    /** Set by the screen: whether this device can offer fingerprint or face unlock. */
    var offerBiometrics by mutableStateOf(false)

    private var firstPin: String? = null

    init {
        // Prefill the profile (for example after a restore, where the PIN is not carried over).
        if (savedStateHandle.get<String>(KEY_NAME) == null) {
            viewModelScope.launch {
                val prefs = preferencesRepository.preferences.first()
                if (name.isEmpty()) name = prefs.technicianName
                if (company.isEmpty()) company = prefs.companyName
            }
        }
    }

    fun onGetStarted() = go(SetupStep.Profile)

    fun onNameChange(value: String) {
        name = value
        savedStateHandle[KEY_NAME] = value
        if (error == SetupError.NameRequired || error == SetupError.NameTooLong) error = null
    }

    fun onCompanyChange(value: String) {
        company = value
        savedStateHandle[KEY_COMPANY] = value
        if (error == SetupError.CompanyTooLong) error = null
    }

    fun onProfileContinue() {
        val cleanName = ProfileRules.sanitize(name, ProfileRules.TECHNICIAN_NAME_MAX_LENGTH)
        error = when {
            cleanName == null -> SetupError.NameTooLong
            cleanName.isEmpty() -> SetupError.NameRequired
            ProfileRules.sanitize(company, ProfileRules.COMPANY_NAME_MAX_LENGTH) == null -> SetupError.CompanyTooLong
            else -> null
        }
        if (error == null) go(SetupStep.CreatePin)
    }

    fun onDigit(digit: Char) {
        if (isSaving || (step != SetupStep.CreatePin && step != SetupStep.ConfirmPin)) return
        pin = pin.plus(digit)
        if (pin.digits.isNotEmpty() && error != SetupError.SaveFailed) error = null
        if (!pin.isComplete) return
        when (step) {
            SetupStep.CreatePin -> when (PinRules.validate(pin.digits)) {
                PinError.TOO_SIMPLE -> rejectPin(SetupError.PinTooSimple)
                PinError.FORMAT -> rejectPin(null)
                null -> {
                    firstPin = pin.digits
                    pin = PinEntry()
                    go(SetupStep.ConfirmPin)
                }
            }
            SetupStep.ConfirmPin -> if (pin.digits == firstPin) {
                if (offerBiometrics) {
                    go(SetupStep.Biometric)
                } else {
                    pin = PinEntry()
                    finish(useBiometrics = false)
                }
            } else {
                firstPin = null
                rejectPin(SetupError.PinMismatch)
                go(SetupStep.CreatePin)
            }
            else -> Unit
        }
    }

    fun onBackspace() {
        pin = pin.backspace()
    }

    /** Back arrow and system back. Returns false on the first page (let the system handle it). */
    fun onBack(): Boolean {
        error = null
        pin = PinEntry()
        firstPin = null
        val previous = when (step) {
            SetupStep.Welcome -> return false
            SetupStep.Profile -> SetupStep.Welcome
            SetupStep.CreatePin, SetupStep.ConfirmPin -> SetupStep.Profile
            SetupStep.Biometric -> SetupStep.CreatePin
        }
        go(previous)
        return true
    }

    /** Stores the profile, the optional biometric choice and the PIN, then opens the app. */
    fun finish(useBiometrics: Boolean) {
        val chosenPin = firstPin ?: return go(SetupStep.CreatePin)
        if (isSaving) return
        isSaving = true
        viewModelScope.launch {
            try {
                preferencesRepository.setTechnicianName(ProfileRules.sanitize(name, ProfileRules.TECHNICIAN_NAME_MAX_LENGTH).orEmpty())
                preferencesRepository.setCompanyName(ProfileRules.sanitize(company, ProfileRules.COMPANY_NAME_MAX_LENGTH).orEmpty())
                lockRepository.setBiometricEnabled(useBiometrics)
                // Unlock first: the app opens as soon as the PIN exists, without asking for it again.
                session.unlock()
                lockRepository.setPin(chosenPin)
                firstPin = null
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                session.lock()
                error = SetupError.SaveFailed
            } finally {
                isSaving = false
            }
        }
    }

    private fun rejectPin(reason: SetupError?) {
        pin = PinEntry()
        error = reason
    }

    private fun go(next: SetupStep) {
        step = next
        savedStateHandle[KEY_STEP] = next.name
    }

    private companion object {
        const val KEY_STEP = "setup.step"
        const val KEY_NAME = "setup.name"
        const val KEY_COMPANY = "setup.company"

        /** After process death the typed PIN is gone, so PIN steps resume at "create PIN". */
        fun SetupStep.coerceBeforePin() = if (this == SetupStep.ConfirmPin || this == SetupStep.Biometric) SetupStep.CreatePin else this
    }
}
