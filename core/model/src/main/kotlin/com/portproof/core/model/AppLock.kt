package com.portproof.core.model

import java.time.Duration
import java.time.Instant
import kotlin.math.min

/**
 * Rules for the app lock (the PortProof login). The PIN never leaves the device: PortProof has no
 * account and no server, so a forgotten PIN cannot be recovered, only reset by erasing app data.
 */
object PinRules {
    const val LENGTH = 6

    fun validate(pin: String): PinError? = when {
        pin.length != LENGTH || pin.any { it !in '0'..'9' } -> PinError.FORMAT
        pin.all { it == pin[0] } -> PinError.TOO_SIMPLE
        isStraightSequence(pin) -> PinError.TOO_SIMPLE
        else -> null
    }

    /** 123456, 456789, 987654 and similar runs. */
    private fun isStraightSequence(pin: String): Boolean {
        val steps = pin.zipWithNext { a, b -> b - a }.toSet()
        return steps == setOf(1) || steps == setOf(-1)
    }
}

enum class PinError {
    /** Not exactly [PinRules.LENGTH] digits. */
    FORMAT,

    /** All the same digit, or a straight run such as 123456. */
    TOO_SIMPLE,
}

/** Wrong-PIN throttling. The counter survives app restarts. */
object LockoutPolicy {
    /** Wrong attempts allowed before the first wait. */
    const val FREE_ATTEMPTS = 5

    private val FIRST_WAIT: Duration = Duration.ofSeconds(30)
    private const val MAX_DOUBLINGS = 5

    /** Wait imposed after [failedAttempts] consecutive wrong PINs: 30 s, then doubling up to 16 min. */
    fun waitAfter(failedAttempts: Int): Duration? {
        if (failedAttempts < FREE_ATTEMPTS) return null
        val doublings = min(failedAttempts - FREE_ATTEMPTS, MAX_DOUBLINGS)
        return FIRST_WAIT.multipliedBy(1L shl doublings)
    }
}

object AppLockRules {
    /** The app locks again when it returns after at least this long in the background. */
    val BACKGROUND_TIMEOUT: Duration = Duration.ofMinutes(5)
}

/** Stored app-lock state (never the PIN itself). */
data class AppLockSettings(
    val pinSet: Boolean,
    val biometricEnabled: Boolean,
    val failedAttempts: Int,
    val lockedUntil: Instant?,
) {
    companion object {
        val None = AppLockSettings(pinSet = false, biometricEnabled = false, failedAttempts = 0, lockedUntil = null)
    }
}

sealed interface PinCheck {
    data object Correct : PinCheck

    /** [attemptsLeft] wrong attempts remain before a wait is imposed. */
    data class Wrong(val attemptsLeft: Int) : PinCheck

    data class LockedOut(val until: Instant) : PinCheck
}
