package com.portproof.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.portproof.core.data.repository.DataStoreUserPreferencesRepository
import com.portproof.core.model.ThemeMode
import com.portproof.core.model.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

class DataStoreUserPreferencesRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun defaultsThenPersistsValues() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            tmp.newFile("user_preferences.preferences_pb")
        }
        val repository = DataStoreUserPreferencesRepository(dataStore)
        assertEquals(UserPreferences(), repository.preferences.first())

        repository.setTechnicianName("Sam Taylor")
        repository.setCompanyName("Taylor Systems")
        repository.setThemeMode(ThemeMode.DARK)

        assertEquals(UserPreferences("Sam Taylor", "Taylor Systems", ThemeMode.DARK), repository.preferences.first())
    }
}
