package com.portproof.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.portproof.app.R
import com.portproof.app.navigation.PortProofAppState
import com.portproof.app.navigation.PortProofNavHost
import com.portproof.core.designsystem.theme.PortProofTheme
import kotlinx.coroutines.launch

/**
 * App shell. The reference design has no tab bar: Home is the hub and every screen manages its own
 * header and bottom actions. The shell only hosts navigation and app-wide snackbars.
 */
@Composable
fun PortProofApp(appState: PortProofAppState) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.project_deleted_message)
    Box(
        Modifier
            .fillMaxSize()
            .background(PortProofTheme.colors.background),
    ) {
        PortProofNavHost(
            appState = appState,
            onProjectDeleted = { scope.launch { snackbarHostState.showSnackbar(deletedMessage) } },
            modifier = Modifier.fillMaxSize(),
        )
        SnackbarHost(
            snackbarHostState,
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 16.dp),
        )
    }
}
