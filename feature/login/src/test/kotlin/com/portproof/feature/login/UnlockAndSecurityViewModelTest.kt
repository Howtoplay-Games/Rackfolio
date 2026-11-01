package com.portproof.feature.login

import com.portproof.core.data.repository.AppLockSession
import com.portproof.core.model.PinCheck
import com.portproof.core.model.UserPreferences
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.MutableTestClock
import com.portproof.core.testing.TestAppLockRepository
import com.portproof.core.testing.TestUserPreferencesRepository
import com.portproof.feature.login.security.ChangePinStep
import com.portproof.feature.login.security.SecurityMessage
import com.portproof.feature.login.security.SecurityViewModel
import com.portproof.feature.login.unlock.UnlockFeedback
import com.portproof.feature.login.unlock.UnlockViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnlockAndSecurityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = MutableTestClock()
    private val locks = TestAppLockRepository(clock, pin = "246813")
    private val session = AppLockSession(clock)
    private val preferences = TestUserPreferencesRepository(UserPreferences(technicianName = "Nick Turner"))

    private fun type(pin: String, onDigit: (Char) -> Unit) = pin.forEach(onDigit)

    @Test
    fun correctPinUnlocksAndWrongPinsCountDownToAWait() = runTest {
        val vm = UnlockViewModel(locks, session, preferences)
        advanceUntilIdle()
        assertEquals("Nick Turner", vm.uiState.value.technicianName)

        type("111112", vm::onDigit)
        advanceUntilIdle()
        assertEquals(UnlockFeedback.Wrong(attemptsLeft = 4), vm.feedback)
        assertFalse(session.unlocked.value)
        assertEquals(0, vm.pin.digits.length)

        repeat(4) { type("111112", vm::onDigit); advanceUntilIdle() }
        assertNotNull(vm.uiState.value.lockedUntil)
        type("246813", vm::onDigit)
        advanceUntilIdle()
        assertFalse(session.unlocked.value) // still waiting

        clock.now = clock.now.plusSeconds(31)
        type("246813", vm::onDigit)
        advanceUntilIdle()
        assertTrue(session.unlocked.value)
    }

    @Test
    fun fingerprintUnlockClearsTheWrongAttemptCount() = runTest {
        val vm = UnlockViewModel(locks, session, preferences)
        type("111112", vm::onDigit)
        advanceUntilIdle()
        vm.onBiometricSuccess()
        advanceUntilIdle()
        assertTrue(session.unlocked.value)
        assertEquals(0, locks.settings.first().failedAttempts)
    }

    @Test
    fun appLocksAgainAfterFiveMinutesInTheBackground() {
        session.unlock()
        session.onAppBackgrounded()
        clock.now = clock.now.plusSeconds(4 * 60)
        session.onAppForegrounded()
        assertTrue(session.unlocked.value)

        session.onAppBackgrounded()
        clock.now = clock.now.plusSeconds(5 * 60)
        session.onAppForegrounded()
        assertFalse(session.unlocked.value)
    }

    @Test
    fun changingThePinChecksTheCurrentOneAndConfirmsTheNewOne() = runTest {
        val vm = SecurityViewModel(locks, session)
        vm.startChangePin()
        type("000001", vm::onDigit)
        advanceUntilIdle()
        assertEquals(SecurityMessage.WrongPin, vm.message)
        assertEquals(ChangePinStep.Current, vm.changeStep)

        type("246813", vm::onDigit)
        advanceUntilIdle()
        assertEquals(ChangePinStep.New, vm.changeStep)
        type("555555", vm::onDigit)
        assertEquals(SecurityMessage.TooSimple, vm.message)
        type("975318", vm::onDigit)
        assertEquals(ChangePinStep.Confirm, vm.changeStep)
        type("975310", vm::onDigit)
        assertEquals(SecurityMessage.Mismatch, vm.message)
        assertEquals(ChangePinStep.New, vm.changeStep)

        type("975318", vm::onDigit)
        type("975318", vm::onDigit)
        advanceUntilIdle()
        assertEquals(SecurityMessage.PinChanged, vm.message)
        assertNull(vm.changeStep)
        assertEquals(PinCheck.Correct, locks.checkPin("975318"))
    }

    @Test
    fun lockNowLocksTheSession() {
        session.unlock()
        SecurityViewModel(locks, session).lockNow()
        assertFalse(session.unlocked.value)
    }
}
