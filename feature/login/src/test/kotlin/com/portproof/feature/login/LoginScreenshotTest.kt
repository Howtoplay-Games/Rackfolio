package com.portproof.feature.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.AppLockSettings
import com.portproof.core.testing.saveScreenshot
import com.portproof.feature.login.security.SecurityScreen
import com.portproof.feature.login.unlock.UnlockFeedback
import com.portproof.feature.login.unlock.UnlockScreen
import com.portproof.feature.login.unlock.UnlockUiState
import com.portproof.feature.login.welcome.WelcomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders the welcome pages, PIN setup, the login screen and Login & security for design review. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xxhdpi")
class LoginScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun render(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent { PortProofTheme(darkTheme = dark) { content() } }
        compose.saveScreenshot(name)
    }

    @Test
    fun welcomeFirstPage() = render("login_welcome_1") { WelcomeScreen(onGetStarted = {}) }

    @Test
    fun welcomeSecondPage() = render("login_welcome_2") { WelcomeScreen(onGetStarted = {}, initialPage = 1) }

    @Test
    fun welcomeLastPage() = render("login_welcome_3") { WelcomeScreen(onGetStarted = {}, initialPage = 2) }

    @Test
    fun welcomeDark() = render("login_welcome_dark", dark = true) { WelcomeScreen(onGetStarted = {}) }

    @Test
    fun choosePin() = render("login_setup_pin") {
        LoginScaffold(title = "Choose a 6-digit PIN", body = "You\u2019ll enter it each time you open PortProof.", onBack = {}) {
            PinPad(entered = 4, message = null, isError = false, enabled = true, onDigit = {}, onBackspace = {})
        }
    }

    @Test
    fun unlock() = render("login_unlock") {
        UnlockScreen(
            state = UnlockUiState(technicianName = "Nick Turner"),
            entered = 2,
            feedback = UnlockFeedback.Wrong(attemptsLeft = 3),
            checking = false,
            onDigit = {},
            onBackspace = {},
            onBiometric = {},
        )
    }

    @Test
    fun unlockDark() = render("login_unlock_dark", dark = true) {
        UnlockScreen(UnlockUiState(technicianName = "Nick Turner"), 0, null, false, {}, {}, onBiometric = null)
    }

    @Test
    fun security() = render("login_security") {
        SecurityScreen(
            settings = AppLockSettings(pinSet = true, biometricEnabled = false, failedAttempts = 0, lockedUntil = null),
            onBack = {},
            onChangePin = {},
            onBiometricChange = {},
            onLockNow = {},
        )
    }
}
