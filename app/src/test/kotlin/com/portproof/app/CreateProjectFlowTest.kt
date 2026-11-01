package com.portproof.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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

    @Test
    fun createProjectFromDashboardThenFindItEverywhere() {
        waitForText("Create Your First Project")
        compose.saveScreenshot("01_dashboard_first_run")

        compose.onNodeWithText("Create Your First Project").performClick()
        waitForText("New project")

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

        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitForText("Active Projects")
        // Featured under "Active Projects" on the dashboard.
        compose.onAllNodesWithText("Warehouse CCTV upgrade").onFirst().performScrollTo().assertIsDisplayed()
        compose.saveScreenshot("04_dashboard_with_project")

        compose.onNodeWithContentDescription("Projects").performClick()
        waitForText("1 project")
        compose.onNodeWithText("Warehouse CCTV upgrade").assertIsDisplayed()
        compose.saveScreenshot("05_projects_list")
    }

    @Test
    fun leavingEditorWithUnsavedChangesAsksBeforeDiscarding() {
        waitForText("Create Your First Project")
        compose.onNodeWithText("Create Your First Project").performClick()
        waitForText("New project")
        compose.onNode(hasText("Project name *") and hasSetTextAction()).performTextInput("Plant room BMS")
        compose.waitForIdle() // let the back handler observe the unsaved change

        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitForText("Discard changes?")
        compose.saveScreenshot("06_discard_dialog")

        compose.onNodeWithText("Keep editing").performClick()
        compose.onNodeWithText("Plant room BMS").assertIsDisplayed()
    }

    @Test
    fun everyHomeEntryPointAndTabOpensItsScreen() {
        waitForText("Create Your First Project")

        compose.onNodeWithContentDescription("Activity").performClick()
        waitForText("No activity yet")
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitForText("Overview")

        compose.onAllNodesWithText("See Details").onFirst().performClick()
        waitForText("Activity by Hour")
        compose.onNodeWithText("Upcoming Deadlines").assertIsDisplayed()
        compose.saveScreenshot("07_insight_empty")
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitForText("Overview")

        compose.onNodeWithContentDescription("Insights").performClick()
        waitForText("Total Projects") // content, not just the title (the title also shows while loading)
        compose.saveScreenshot("08_metrics_empty")

        compose.onNodeWithContentDescription("Search").performClick()
        waitForText("Find a project")
        compose.saveScreenshot("09_search")

        // Back from a tab returns to Home before leaving the app.
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitForText("Overview")
    }
}
