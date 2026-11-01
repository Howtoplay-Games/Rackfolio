package com.portproof.core.data.repository

import com.portproof.core.model.AppLockSettings
import com.portproof.core.model.PinCheck
import kotlinx.coroutines.flow.Flow

/**
 * The PortProof login: a 6-digit PIN stored only as a salted PBKDF2 hash in app-private storage,
 * plus optional fingerprint or face unlock. Excluded from backups, so a restored device asks for a
 * new PIN.
 */
interface AppLockRepository {
    val settings: Flow<AppLockSettings>

    /** Replaces the PIN. Throws [IllegalArgumentException] when it breaks [com.portproof.core.model.PinRules]. */
    suspend fun setPin(pin: String)

    /** Checks [pin], counting wrong attempts and imposing waits per [com.portproof.core.model.LockoutPolicy]. */
    suspend fun checkPin(pin: String): PinCheck

    suspend fun setBiometricEnabled(enabled: Boolean)

    /** Clears wrong-attempt counting after the user authenticated another way (biometrics). */
    suspend fun clearFailedAttempts()
}
