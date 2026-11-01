package com.portproof.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.portproof.core.model.ThemeMode
import com.portproof.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

internal class DataStoreUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data
        // A read failure falls back to defaults rather than blocking the app.
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs ->
            UserPreferences(
                technicianName = prefs[TECHNICIAN_NAME].orEmpty(),
                companyName = prefs[COMPANY_NAME].orEmpty(),
                themeMode = ThemeMode.entries.firstOrNull { it.name == prefs[THEME_MODE] } ?: ThemeMode.SYSTEM,
            )
        }
        .distinctUntilChanged()

    override suspend fun setTechnicianName(name: String) {
        dataStore.edit { it[TECHNICIAN_NAME] = name }
    }

    override suspend fun setCompanyName(name: String) {
        dataStore.edit { it[COMPANY_NAME] = name }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    private companion object {
        val TECHNICIAN_NAME = stringPreferencesKey("technician_name")
        val COMPANY_NAME = stringPreferencesKey("company_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
