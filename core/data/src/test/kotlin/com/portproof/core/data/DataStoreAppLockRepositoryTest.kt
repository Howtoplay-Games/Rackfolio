package com.portproof.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.portproof.core.data.repository.DataStoreAppLockRepository
import com.portproof.core.data.security.PinHasher
import com.portproof.core.model.PinCheck
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DataStoreAppLockRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val clock = TestClock()

    // A low iteration count keeps the tests fast; production uses PinHasher.DEFAULT_ITERATIONS.
    private fun TestScope.repository(file: String = "app_lock.preferences_pb") = DataStoreAppLockRepository(
        PreferenceDataStoreFactory.create(scope = backgroundScope) { java.io.File(tmp.root, file) },
        clock,
        PinHasher(iterations = 1_000),
    )

    @Test
    fun correctPinUnlocksAndWrongPinCountsDown() = runTest {
        val locks = repository()
        assertFalse(locks.settings.first().pinSet)

        locks.setPin("246813")
        assertTrue(locks.settings.first().pinSet)
        assertEquals(PinCheck.Correct, locks.checkPin("246813"))
        assertEquals(PinCheck.Wrong(attemptsLeft = 4), locks.checkPin("246810"))
        assertEquals(PinCheck.Wrong(attemptsLeft = 3), locks.checkPin("000001"))

        // A correct PIN resets the counter.
        assertEquals(PinCheck.Correct, locks.checkPin("246813"))
        assertEquals(0, locks.settings.first().failedAttempts)
    }

    @Test
    fun fifthWrongPinImposesAWaitThatDoublesAndSurvivesRestart() = runTest {
        val locks = repository()
        locks.setPin("246813")
        repeat(4) { assertIs<PinCheck.Wrong>(locks.checkPin("111112")) }

        val first = assertIs<PinCheck.LockedOut>(locks.checkPin("111112"))
        assertEquals(clock.now.plusSeconds(30), first.until)
        // Even the correct PIN is refused while the wait runs. The wait is stored, not held in memory.
        assertEquals(first, locks.checkPin("246813"))
        assertEquals(first.until, locks.settings.first().lockedUntil)

        clock.now = first.until
        val second = assertIs<PinCheck.LockedOut>(locks.checkPin("111112"))
        assertEquals(Duration.ofSeconds(60), Duration.between(clock.now, second.until))

        clock.now = second.until
        assertEquals(PinCheck.Correct, locks.checkPin("246813"))
        assertEquals(null, locks.settings.first().lockedUntil)
    }

    @Test
    fun weakPinsAreRejectedAndHashesAreSalted() = runTest {
        val locks = repository()
        assertFailsWith<IllegalArgumentException> { locks.setPin("123456") }
        assertFailsWith<IllegalArgumentException> { locks.setPin("777777") }
        assertFailsWith<IllegalArgumentException> { locks.setPin("12345") }
        assertFalse(locks.settings.first().pinSet)

        val hasher = PinHasher(iterations = 1_000)
        assertNotEquals(hasher.hash("246813", hasher.newSalt()).toList(), hasher.hash("246813", hasher.newSalt()).toList())
    }

    @Test
    fun biometricSettingIsStored() = runTest {
        val locks = repository()
        locks.setPin("246813")
        locks.setBiometricEnabled(true)
        assertTrue(locks.settings.first().biometricEnabled)
        locks.setBiometricEnabled(false)
        assertFalse(locks.settings.first().biometricEnabled)
    }
}
