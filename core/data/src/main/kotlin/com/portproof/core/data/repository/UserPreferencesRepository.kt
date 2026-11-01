package com.portproof.core.data.repository

import com.portproof.core.model.ThemeMode
import com.portproof.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    /** Values must already be sanitised with [com.portproof.core.model.ProfileRules]. */
    suspend fun setTechnicianName(name: String)
    suspend fun setCompanyName(name: String)
    suspend fun setThemeMode(mode: ThemeMode)
}
