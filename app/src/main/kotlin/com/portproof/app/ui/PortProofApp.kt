package com.portproof.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.portproof.app.R
import com.portproof.app.navigation.PortProofAppState
import com.portproof.app.navigation.PortProofNavHost
import com.portproof.app.navigation.TopLevelDestination
import com.portproof.core.designsystem.component.PpFloatingNavBar
import com.portproof.core.designsystem.component.PpFloatingNavBarReservedHeight
import com.portproof.core.designsystem.component.PpNavItem
import com.portproof.core.designsystem.component.PpNavigationRail
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.ui.LocalBottomBarInset
import kotlinx.coroutines.launch
import com.portproof.core.designsystem.R as DsR

/** Below 600dp: floating bottom bar on top-level screens. From 600dp: a persistent navigation rail. */
@Composable
fun PortProofApp(appState: PortProofAppState) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.project_deleted_message)
    val navItems = TopLevelDestination.entries.map { PpNavItem(it.name, stringResource(it.labelRes), it.icon) }
    val onSelect: (PpNavItem) -> Unit = { item -> appState.navigateToTopLevel(TopLevelDestination.valueOf(item.key)) }
    val topLevel = appState.currentTopLevelDestination

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(PortProofTheme.colors.background),
    ) {
        val useRail = maxWidth >= 600.dp
        val showFloatingBar = !useRail && topLevel != null
        val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Row(Modifier.fillMaxSize()) {
            if (useRail) {
                PpNavigationRail(
                    items = navItems,
                    selectedKey = appState.selectedTopLevelDestination.name,
                    onSelect = onSelect,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)),
                )
            }
            Box(Modifier.weight(1f)) {
                CompositionLocalProvider(
                    LocalBottomBarInset provides if (showFloatingBar) PpFloatingNavBarReservedHeight + navBarInset else navBarInset,
                ) {
                    PortProofNavHost(
                        appState = appState,
                        onProjectDeleted = { scope.launch { snackbarHostState.showSnackbar(deletedMessage) } },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                FloatingNavBarHost(
                    visible = showFloatingBar,
                    items = navItems,
                    selectedKey = topLevel?.name,
                    onSelect = onSelect,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
                SnackbarHost(
                    snackbarHostState,
                    Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = if (showFloatingBar) PpFloatingNavBarReservedHeight else 8.dp),
                )
            }
        }
    }
}

@Composable
private fun FloatingNavBarHost(
    visible: Boolean,
    items: List<PpNavItem>,
    selectedKey: String?,
    onSelect: (PpNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
    ) {
        PpFloatingNavBar(
            items = items,
            selectedKey = selectedKey,
            onSelect = onSelect,
            navigationLabel = stringResource(DsR.string.designsystem_navigation),
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 16.dp),
        )
    }
}
