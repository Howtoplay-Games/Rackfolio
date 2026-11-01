package com.portproof.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ProjectSummary
import com.portproof.core.testing.saveScreenshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders the home screen with representative data for design review (build/screenshots). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class DashboardScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val state = DashboardUiState(
        isLoading = false,
        technicianName = "Nick Turner",
        summary = ProjectSummary(
            total = 20, active = 14, completed = 6, archived = 3, planned = 4, inProgress = 9, onHold = 1,
            createdThisMonth = 3, completedThisMonth = 2,
        ),
        today = java.time.LocalDate.of(2026, 9, 7),
    )

    private val noActions = DashboardActions({}, {}, {}, {}, {})

    private fun render(dark: Boolean, name: String, data: DashboardUiState = state) {
        compose.setContent {
            PortProofTheme(darkTheme = dark) {
                Box(Modifier.fillMaxSize().background(PortProofTheme.colors.background)) { DashboardScreen(data, noActions) }
            }
        }
        compose.saveScreenshot(name)
    }

    @Test
    @Config(qualifiers = "w390dp-h844dp-xxhdpi")
    fun homeLight() = render(dark = false, name = "home_light")

    @Test
    @Config(qualifiers = "w390dp-h844dp-xxhdpi")
    fun homeDark() = render(dark = true, name = "home_dark")

    @Test
    @Config(qualifiers = "w390dp-h844dp-xxhdpi")
    fun homeFirstRun() = render(dark = false, name = "home_first_run", data = DashboardUiState(isLoading = false, today = state.today))

    @Test
    @Config(qualifiers = "w1280dp-h800dp-mdpi")
    fun homeTablet() = render(dark = false, name = "home_tablet")
}
