package com.portproof.app

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.testing.saveScreenshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * End-to-end flows through the real app: Hilt graph, Room database, DataStore and the real Google
 * Play Billing client (which reports billing as unavailable under Robolectric, so the free plan applies).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CreateProjectFlowTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String, substring: Boolean = false) =
        compose.waitUntilAtLeastOneExists(hasText(text, substring = substring), timeoutMillis = 10_000)

    private fun waitForDescription(text: String) = compose.waitUntilAtLeastOneExists(hasContentDescription(text), 10_000)

    private fun back() = compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

    private fun click(text: String) = compose.onNode(hasText(text) and hasClickAction()).performClick()

    private fun typePin(pin: String) = pin.forEach { click(it.toString()) }

    private fun waitForHome() = waitForText("Dashboard")

    private fun exists(text: String) = compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()

    /**
     * Opens a bottom-bar tab through its accessibility click action, which runs the same click
     * handler as a touch but starts no press ripple. Robolectric never finishes ripple animations,
     * so a touch would leave a frozen highlight on the bar in the next render.
     */
    private fun openTab(label: String) =
        compose.onNodeWithContentDescription(label).performSemanticsAction(SemanticsActions.OnClick)

    /** First run: welcome pages, name, PIN twice, then "Not now" if fingerprint unlock is offered. */
    private fun completeLoginSetup(screenshots: Boolean = false) {
        waitForText("Every install,", substring = true)
        if (screenshots) compose.saveScreenshot("01_welcome")
        click("Next")
        click("Next")
        waitForText("Get Started")
        click("Get Started")
        waitForText("Create your login")
        compose.onNode(hasText("Your name *") and hasSetTextAction()).performTextInput("Nick Turner")
        click("Continue")
        waitForText("Choose a 6-digit PIN")
        typePin("123456")
        waitForText("Avoid repeated or sequential digits", substring = true)
        if (screenshots) compose.saveScreenshot("02_login_pin_rule")
        typePin(PIN)
        waitForText("Enter your PIN again")
        typePin(PIN)
        // Robolectric reports an enrolled fingerprint, so setup offers fingerprint unlock.
        compose.waitUntil(10_000) { exists("Not now") || exists("Dashboard") }
        if (exists("Not now")) {
            if (screenshots) compose.saveScreenshot("03_login_fingerprint")
            click("Not now")
        }
        waitForHome()
    }

    private fun createProject(name: String) {
        compose.onNodeWithContentDescription("New project").performClick()
        waitForText("Create project")
        compose.onNode(hasText("Project name *") and hasSetTextAction()).performTextInput(name)
        click("Create project")
        waitForText("Project created")
        compose.onNodeWithContentDescription("Close and return home").performClick()
        waitForHome()
    }

    @Test
    fun firstRunSetsUpTheLoginThenCreatesAProjectAndFindsIt() {
        completeLoginSetup(screenshots = true)
        compose.saveScreenshot("04_home_first_run")

        compose.onNodeWithContentDescription("New project").performClick()
        waitForText("Create project")
        // Saving without a name is blocked with an inline error.
        click("Create project")
        waitForText("Enter a project name")
        compose.saveScreenshot("05_editor_validation")

        compose.onNode(hasText("Project name *") and hasSetTextAction()).performTextInput("Warehouse CCTV upgrade")
        compose.onNode(hasText("Customer name") and hasSetTextAction()).performTextInput("Northwind Logistics")
        click("In progress")
        click("Create project")
        waitForText("Project created")
        compose.onNodeWithText("Warehouse CCTV upgrade").assertIsDisplayed()
        compose.saveScreenshot("06_project_detail")

        // The header close button returns straight to Home, which now shows the project card.
        compose.onNodeWithContentDescription("Close and return home").performClick()
        waitForHome()
        waitForText("Warehouse CCTV upgrade")
        compose.saveScreenshot("07_home_with_project")

        compose.onNodeWithContentDescription("Projects").performClick()
        waitForText("All projects")
        waitForText("Warehouse CCTV upgrade")
        compose.onNodeWithText("Warehouse CCTV upgrade").assertIsDisplayed()
        compose.saveScreenshot("08_projects_list")
    }

    @Test
    fun leavingEditorWithUnsavedChangesAsksBeforeDiscarding() {
        completeLoginSetup()
        compose.onNodeWithContentDescription("New project").performClick()
        waitForText("Create project")
        compose.onNode(hasText("Project name *") and hasSetTextAction()).performTextInput("Plant room BMS")
        compose.waitForIdle() // let the back handler observe the unsaved change

        back()
        waitForText("Discard changes?")
        compose.saveScreenshot("09_discard_dialog")

        click("Keep editing")
        compose.onNodeWithText("Plant room BMS").assertIsDisplayed()
    }

    @Test
    fun everyTabAndHeaderButtonOpensItsScreenAndLockNowAsksForThePin() {
        completeLoginSetup()

        compose.onNodeWithContentDescription("Recent activity").performClick()
        waitForText("No activity yet", substring = true)
        back()
        waitForHome()
        compose.onNodeWithContentDescription("Projects by start date").performClick()
        waitForText("Active projects")

        openTab("Search")
        waitForText("Find a project")
        compose.saveScreenshot("10_search")

        openTab("Overview")
        waitForText("Upcoming deadlines")
        compose.saveScreenshot("11_overview")

        openTab("Settings")
        waitForText("Free plan")
        compose.saveScreenshot("12_settings")

        click("See PortProof Pro")
        waitForText("Choose a plan")
        waitForText("Treat this device as Pro") // debug builds only
        compose.saveScreenshot("13_pro")
        compose.onNodeWithContentDescription("Close").performClick()
        waitForText("Free plan")

        click("Login & security")
        waitForText("Lock now")
        click("Lock now")
        waitForText("Welcome back,", substring = true)
        compose.saveScreenshot("14_login")
        typePin("111111")
        waitForText("Wrong PIN", substring = true)
        typePin(PIN)
        waitForText("Lock now")

        // Back from a tab returns Home before leaving the app.
        back()
        waitForText("Free plan")
        back()
        waitForHome()
    }

    @Test
    fun freePlanStopsAtThreeActiveProjectsAndPointsToPro() {
        completeLoginSetup()
        createProject("Site one")
        createProject("Site two")
        createProject("Site three")

        compose.onNodeWithContentDescription("New project").performClick()
        waitForText("The free plan includes 3 active projects", substring = true)
        compose.saveScreenshot("15_free_plan_limit")
        compose.onNode(hasText("Project name *") and hasSetTextAction()).performTextInput("Site four")
        click("Create project")
        waitForText("See Pro")
        click("See Pro")
        waitForText("Choose a plan")
    }

    private companion object {
        const val PIN = "246813"
    }
}
