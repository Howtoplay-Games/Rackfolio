package com.portproof.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.portproof.core.model.ProjectListFilter
import com.portproof.feature.dashboard.navigation.HomeRoute
import com.portproof.feature.projects.navigation.ProjectsRoute
import com.portproof.feature.settings.navigation.SettingsRoute

@Composable
fun rememberPortProofAppState(navController: NavHostController = rememberNavController()): PortProofAppState =
    remember(navController) { PortProofAppState(navController) }

/**
 * Navigation policy for the whole app. Each top-level destination keeps its own back stack
 * (saved/restored on tab switches); the start destination (Home) stays at the bottom so Back from
 * any tab returns Home before leaving the app.
 */
@Stable
class PortProofAppState(val navController: NavHostController) {

    /** The last top-level destination shown; drives the rail selection on detail screens. */
    private var lastTopLevel by mutableStateOf(TopLevelDestination.HOME)

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    /** Non-null only while a top-level destination is on screen (the floating bar is shown then). */
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val destination = currentDestination ?: return null
            return TopLevelDestination.entries.firstOrNull { destination.hasRoute(it.route) }?.also { lastTopLevel = it }
        }

    val selectedTopLevelDestination: TopLevelDestination
        @Composable get() = currentTopLevelDestination ?: lastTopLevel

    fun navigateToTopLevel(destination: TopLevelDestination) {
        val options = navOptions {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        when (destination) {
            TopLevelDestination.HOME -> navController.navigate(HomeRoute, options)
            TopLevelDestination.PROJECTS -> navController.navigate(ProjectsRoute(), options)
            TopLevelDestination.SETTINGS -> navController.navigate(SettingsRoute, options)
        }
    }

    /** Opens the Projects tab with [filter] preselected (from dashboard cards and quick actions). */
    fun openProjects(filter: ProjectListFilter) {
        navController.navigate(ProjectsRoute(filter.code)) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
        }
    }

    /**
     * Pops only when the current entry is resumed, so a double tap on a back button during a
     * transition cannot pop two screens.
     */
    fun navigateBack() {
        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            navController.popBackStack()
        }
    }
}
