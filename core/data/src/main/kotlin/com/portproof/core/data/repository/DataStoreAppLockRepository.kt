package com.portproof.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.portproof.core.data.di.AppLockStore
import com.portproof.core.data.security.PinHasher
import com.portproof.core.model.AppLockSettings
import com.portproof.core.model.LockoutPolicy
import com.portproof.core.model.PinCheck
import com.portproof.core.model.PinRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Instant
import java.util.Base64
import javax.inject.Inject

internal class DataStoreAppLockRepository(
    private val dataStore: DataStore<Preferences>,
    private val clock: Clock,
    private val hasher: PinHasher,
) : AppLockRepository {

    @Inject
    constructor(@AppLockStore dataStore: DataStore<Preferences>, clock: Clock) : this(dataStore, clock, PinHasher())

    override val settings: Flow<AppLockSettings> = dataStore.data
        .map { prefs ->
            AppLockSettings(
                pinSet = prefs[PIN_HASH] != null,
                biometricEnabled = prefs[BIOMETRIC_ENABLED] == true,
                failedAttempts = prefs[FAILED_ATTEMPTS] ?: 0,
                lockedUntil = prefs[LOCKED_UNTIL]?.let(Instant::ofEpochMilli),
            )
        }
        .distinctUntilChanged()

    override suspend fun setPin(pin: String) {
        require(PinRules.validate(pin) == null) { "PIN does not meet the rules" }
        val salt = hasher.newSalt()
        val hash = withContext(Dispatchers.Default) { hasher.hash(pin, salt) }
        dataStore.edit {
            it[PIN_HASH] = encoder.encodeToString(hash)
            it[PIN_SALT] = encoder.encodeToString(salt)
            it[PIN_ITERATIONS] = hasher.iterations
            it.remove(FAILED_ATTEMPTS)
            it.remove(LOCKED_UNTIL)
        }
    }

    override suspend fun checkPin(pin: String): PinCheck {
        val prefs = dataStore.data.first()
        val lockedUntil = prefs[LOCKED_UNTIL]?.let(Instant::ofEpochMilli)
        if (lockedUntil != null && clock.instant().isBefore(lockedUntil)) return PinCheck.LockedOut(lockedUntil)

        val hash = prefs[PIN_HASH]?.let(decoder::decode)
        val salt = prefs[PIN_SALT]?.let(decoder::decode)
        val iterations = prefs[PIN_ITERATIONS] ?: hasher.iterations
        val correct = hash != null && salt != null &&
            withContext(Dispatchers.Default) { hasher.matches(pin, salt, iterations, hash) }
        if (correct) {
            clearFailedAttempts()
            return PinCheck.Correct
        }

        var result: PinCheck = PinCheck.Wrong(0)
        dataStore.edit {
            val failed = (it[FAILED_ATTEMPTS] ?: 0) + 1
            it[FAILED_ATTEMPTS] = failed
            val wait = LockoutPolicy.waitAfter(failed)
            result = if (wait != null) {
                val until = clock.instant().plus(wait)
                it[LOCKED_UNTIL] = until.toEpochMilli()
                PinCheck.LockedOut(until)
            } else {
                PinCheck.Wrong(attemptsLeft = LockoutPolicy.FREE_ATTEMPTS - failed)
            }
        }
        return result
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }

    override suspend fun clearFailedAttempts() {
        dataStore.edit {
            it.remove(FAILED_ATTEMPTS)
            it.remove(LOCKED_UNTIL)
        }
    }

    private companion object {
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val PIN_ITERATIONS = intPreferencesKey("pin_iterations")
        val FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        val LOCKED_UNTIL = longPreferencesKey("locked_until")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val encoder: Base64.Encoder = Base64.getEncoder()
        val decoder: Base64.Decoder = Base64.getDecoder()
    }
}
