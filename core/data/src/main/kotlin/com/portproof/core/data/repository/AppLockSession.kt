package com.portproof.core.data.repository

import com.portproof.core.model.AppLockRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory unlock state for this process. Every app start begins locked, and the app locks again
 * after [AppLockRules.BACKGROUND_TIMEOUT] in the background.
 */
@Singleton
class AppLockSession @Inject constructor(private val clock: Clock) {
    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private var backgroundedAt: Instant? = null

    fun unlock() {
        _unlocked.value = true
    }

    fun lock() {
        _unlocked.value = false
    }

    fun onAppBackgrounded() {
        backgroundedAt = clock.instant()
    }

    fun onAppForegrounded() {
        val since = backgroundedAt ?: return
        backgroundedAt = null
        if (Duration.between(since, clock.instant()) >= AppLockRules.BACKGROUND_TIMEOUT) lock()
    }
}
