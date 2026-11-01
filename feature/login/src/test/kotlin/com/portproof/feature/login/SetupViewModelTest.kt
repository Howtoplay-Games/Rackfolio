package com.portproof.feature.login

import androidx.lifecycle.SavedStateHandle
import com.portproof.core.data.repository.AppLockSession
import com.portproof.core.model.PinCheck
import com.portproof.core.model.UserPreferences
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.MutableTestClock
import com.portproof.core.testing.TestAppLockRepository
import com.portproof.core.testing.TestUserPreferencesRepository
import com.portproof.feature.login.setup.SetupError
import com.portproof.feature.login.setup.SetupStep
import com.portproof.feature.login.setup.SetupViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SetupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = MutableTestClock()
    private val locks = TestAppLockRepository(clock)
    private val session = AppLockSession(clock)
    private val preferences = TestUserPreferencesRepository()

    private fun setup(handle: SavedStateHandle = SavedStateHandle()) = SetupViewModel(handle, locks, session, preferences)

    private fun SetupViewModel.type(pin: String) = pin.forEach(::onDigit)

    @Test
    fun walksFromWelcomeToAStoredPinAndOpensTheApp() = runTest {
        val vm = setup()
        assertEquals(SetupStep.Welcome, vm.step)
        vm.onGetStarted()

        vm.onProfileContinue()
        assertEquals(SetupError.NameRequired, vm.error)
        vm.onNameChange("  Nick   Turner ")
        vm.onCompanyChange("Turner Systems")
        vm.onProfileContinue()
        assertEquals(SetupStep.CreatePin, vm.step)

        vm.type("123456")
        assertEquals(SetupError.PinTooSimple, vm.error)
        assertEquals(0, vm.pin.digits.length)

        vm.type("246813")
        assertEquals(SetupStep.ConfirmPin, vm.step)
        vm.type("246810")
        assertEquals(SetupError.PinMismatch, vm.error)
        assertEquals(SetupStep.CreatePin, vm.step)

        vm.type("246813")
        vm.type("246813")
        advanceUntilIdle()

        assertTrue(session.unlocked.value)
        assertTrue(locks.settings.first().pinSet)
        assertFalse(locks.settings.first().biometricEnabled)
        assertEquals(PinCheck.Correct, locks.checkPin("246813"))
        assertEquals(UserPreferences("Nick Turner", "Turner Systems"), preferences.preferences.first())
    }

    @Test
    fun offersFingerprintWhenAvailableAndStoresTheChoice() = runTest {
        val vm = setup()
        vm.offerBiometrics = true
        vm.onGetStarted()
        vm.onNameChange("Nick")
        vm.onProfileContinue()
        vm.type("135792")
        vm.type("135792")
        assertEquals(SetupStep.Biometric, vm.step)
        assertFalse(locks.settings.first().pinSet)

        vm.finish(useBiometrics = true)
        advanceUntilIdle()
        assertTrue(locks.settings.first().pinSet)
        assertTrue(locks.settings.first().biometricEnabled)
    }

    @Test
    fun backStepsAndProcessDeathNeverKeepAHalfTypedPin() = runTest {
        val handle = SavedStateHandle()
        val vm = setup(handle)
        vm.onGetStarted()
        vm.onNameChange("Nick")
        vm.onProfileContinue()
        vm.type("246813")
        assertEquals(SetupStep.ConfirmPin, vm.step)
        assertTrue(vm.onBack())
        assertEquals(SetupStep.Profile, vm.step)

        // Restored after process death on a PIN step: it restarts at "choose a PIN", name kept.
        handle["setup.step"] = SetupStep.ConfirmPin.name
        val restored = setup(handle)
        assertEquals(SetupStep.CreatePin, restored.step)
        assertEquals("Nick", restored.name)
        assertFalse(handle.keys().any { handle.get<Any>(it) == "246813" })
    }
}
