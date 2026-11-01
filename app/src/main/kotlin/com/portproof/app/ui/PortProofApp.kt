package com.portproof.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.foundation.background
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.portproof.app.R
import com.portproof.app.navigation.PortProofAppState
import com.portproof.app.navigation.PortProofNavHost
import com.portproof.app.navigation.TopLevelDestination
import com.portproof.core.designsystem.component.PpBarCenterItem
import com.portproof.core.designsystem.component.PpBarItem
import com.portproof.core.designsystem.component.PpBottomBar
import com.portproof.core.designsystem.theme.PortProofTheme
import kotlinx.coroutines.launch

/**
 * App shell: the navigation graph with the reference's dark bottom bar under it. The bar is shown on
 * the five top-level screens only; detail and editor screens get the full height.
 */
@Composable
fun PortProofApp(appState: PortProofAppState, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.project_deleted_message)
    val current = appState.currentTopLevelDestination
    Box(modifier.fillMaxSize().background(PortProofTheme.colors.canvasBottom)) {
        Column(Modifier.fillMaxSize()) {
            PortProofNavHost(
                appState = appState,
                onProjectDeleted = { scope.launch { snackbarHostState.showSnackbar(deletedMessage) } },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(if (current != null) Modifier.consumeWindowInsets(WindowInsets.navigationBars) else Modifier),
            )
            if (current != null) {
                PpBottomBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val label = stringResource(destination.labelRes)
                        val onClick = { appState.navigateToTopLevel(destination) }
                        if (destination.center) {
                            PpBarCenterItem(destination.icon, label, selected = destination == current, onClick = onClick)
                        } else {
                            PpBarItem(destination.icon, label, selected = destination == current, onClick = onClick)
                        }
                    }
                }
            }
        }
        SnackbarHost(
            snackbarHostState,
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = if (current != null) 96.dp else 16.dp),
        )
    }
}
