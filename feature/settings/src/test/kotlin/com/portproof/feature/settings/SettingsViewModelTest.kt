package com.portproof.feature.settings

import com.portproof.core.model.ProfileRules
import com.portproof.core.model.ThemeMode
import com.portproof.core.testing.MainDispatcherRule
import com.portproof.core.testing.TestPlanRepository
import com.portproof.core.testing.TestProjectRepository
import com.portproof.core.testing.TestUserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestUserPreferencesRepository()
    // Created lazily so viewModelScope is built after MainDispatcherRule has replaced Dispatchers.Main.
    private val vm by lazy { SettingsViewModel(repository, TestPlanRepository(), TestProjectRepository()) }

    @Test
    fun profileValuesAreSanitisedBeforeSaving() = runTest {
        assertTrue(vm.updateProfile(ProfileField.TECHNICIAN_NAME, "  Sam \u202ETaylor  "))
        assertEquals("Sam Taylor", repository.preferences.first().technicianName)
    }

    @Test
    fun tooLongValuesAreRejectedAndNotStored() = runTest {
        val tooLong = "x".repeat(ProfileRules.COMPANY_NAME_MAX_LENGTH + 1)
        assertFalse(vm.updateProfile(ProfileField.COMPANY_NAME, tooLong))
        assertEquals("", repository.preferences.first().companyName)
    }

    @Test
    fun themeModeIsStored() = runTest {
        vm.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repository.preferences.first().themeMode)
    }
}
