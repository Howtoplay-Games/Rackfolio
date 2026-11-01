package com.portproof.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.portproof.core.testing.saveScreenshot

/**
 * End-to-end flow through the real app: Hilt graph, Room database and DataStore (Robolectric).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CreateProjectFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String) = compose.waitUntilAtLeastOneExists(hasText(text), timeoutMillis = 10_000)

    private fun back() = compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

    private fun waitForHome() = compose.waitUntilAtLeastOneExists(hasContentDescription("Active projects: ", substring = true), 10_000)

    @Test
    fun createProjectFromHomeThenFindItEverywhere() {
        waitForHome()
        compose.saveScreenshot("01_home_first_run")

        compose.onNodeWithContentDescription("New project").performClick()
        waitForText("Create project")

        // Saving without a name is blocked with an inline error.
        compose.onNodeWithText("Create project").performClick()
        waitForText("Enter a project name")
        compose.saveScreenshot("02_editor_validation")

        compose.onNode(hasText("Project name *") and hasSetTextAction()).performTextInput("Warehouse CCTV upgrade")
        compose.onNode(hasText("Customer name") and hasSetTextAction()).performTextInput("Northwind Logistics")
        compose.onNodeWithText("In progress").performClick()
        compose.onNodeWithText("Create project").performClick()

        waitForText("Project created")
        compose.onNodeWithText("Warehouse CCTV upgrade").assertIsDisplayed()
        compose.saveScreenshot("03_project_detail")

        // The header close button returns straight to Home, which now counts the project.
        compose.onNodeWithContentDescription("Close and return home").performClick()
        compose.waitUntilAtLeastOneExists(hasContentDescription("Active projects: 1"), 10_000)
        compose.saveScreenshot("04_home_with_project")

        compose.onNodeWithContentDescription("Active projects: 1").performClick()
        waitForText("1 project")
        compose.onNodeWithText("Warehouse CCTV upgrade").assertIsDisplayed()
        compose.saveScreenshot("05_projects_list")
    }

    @Test
    fun leavingEditorWithUnsavedChangesAsksBeforeDiscarding() {
        waitForHome()
        compose.onNodeWithContentDescription("New project").performClick()
        waitForText("Create project")
        compose.onNode(hasText("Project name *") and hasSetTextAction()).performTextInput("Plant room BMS")
        compose.waitForIdle() // let the back handler observe the unsaved change

        back()
        waitForText("Discard changes?")
        compose.saveScreenshot("06_discard_dialog")

        compose.onNodeWithText("Keep editing").performClick()
        compose.onNodeWithText("Plant room BMS").assertIsDisplayed()
    }

    @Test
    fun everyHomeEntryPointOpensItsScreen() {
        waitForHome()

        compose.onNodeWithContentDescription("Project overview").performClick()
        waitForText("Upcoming deadlines")
        compose.saveScreenshot("07_overview_empty")
        compose.onNodeWithText("See all").performClick()
        waitForText("No activity yet")
        back()
        waitForText("Upcoming deadlines")
        back()
        waitForHome()

        compose.onNodeWithText("Search a project").performClick()
        waitForText("Find a project")
        compose.saveScreenshot("08_search")
        back()
        waitForHome()

        compose.onNodeWithContentDescription("Profile and settings").performClick()
        waitForText("Technician name")
        compose.saveScreenshot("09_settings")
        back()
        waitForHome()

        compose.onNodeWithContentDescription("Completed projects: 0 of 0").performClick()
        waitForText("No projects yet") // first-run empty state, with a create button
    }
}
