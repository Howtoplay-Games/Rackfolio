package com.portproof.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.portproof.app.navigation.rememberPortProofAppState
import com.portproof.app.ui.PortProofApp
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ThemeMode
import com.portproof.feature.login.setup.LoginSetupRoute
import com.portproof.feature.login.unlock.UnlockRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** A [FragmentActivity] because the system biometric prompt needs a fragment host. */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Keep project details out of the Recents thumbnail (Android 13+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) setRecentsScreenshotEnabled(false)

        // Collected here rather than inside Compose so the splash condition never depends on
        // composition timing (the first frame is blocked while the splash is kept on screen).
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState = it }
            }
        }
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = viewModel.onAppForegrounded()
            override fun onStop(owner: LifecycleOwner) = viewModel.onAppBackgrounded()
        })
        // Keep the splash screen until the theme and login state are known (no flash of the wrong screen).
        splashScreen.setKeepOnScreenCondition { uiState is MainActivityUiState.Loading }
        enableEdgeToEdge()

        setContent {
            val darkTheme = shouldUseDarkTheme(uiState)
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                )
                onDispose {}
            }
            PortProofTheme(darkTheme = darkTheme) {
                (uiState as? MainActivityUiState.Ready)?.let { LoginGateContent(it.gate) }
            }
        }
    }
}

/**
 * Until a PIN exists the setup flow replaces the app. Once the app has been opened it stays composed
 * (keeping the user's place) and the login screen covers it whenever it locks.
 */
@Composable
private fun LoginGateContent(gate: LoginGate) {
    if (gate == LoginGate.Setup) {
        LoginSetupRoute()
        return
    }
    var opened by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(gate) { if (gate == LoginGate.Unlocked) opened = true }
    val locked = gate == LoginGate.Locked
    val focusManager = LocalFocusManager.current
    LaunchedEffect(locked) { if (locked) focusManager.clearFocus(force = true) }
    Box(Modifier.fillMaxSize()) {
        if (opened || !locked) {
            val appState = rememberPortProofAppState()
            PortProofApp(appState, if (locked) Modifier.clearAndSetSemantics {} else Modifier)
        }
        if (locked) {
            // A Surface consumes touches, so nothing behind the login screen can be reached.
            Surface(Modifier.fillMaxSize()) { UnlockRoute() }
        }
    }
}

@Composable
private fun shouldUseDarkTheme(state: MainActivityUiState): Boolean = when (state) {
    MainActivityUiState.Loading -> isSystemInDarkTheme()
    is MainActivityUiState.Ready -> when (state.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}
