package com.portproof.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.feature.dashboard.navigation.HomeRoute
import com.portproof.feature.insights.navigation.OverviewRoute
import com.portproof.feature.projects.navigation.ProjectsRoute
import com.portproof.feature.search.navigation.SearchRoute
import com.portproof.feature.settings.navigation.SettingsRoute

@Composable
fun rememberPortProofAppState(navController: NavHostController = rememberNavController()): PortProofAppState =
    remember(navController) { PortProofAppState(navController) }

/**
 * Navigation policy. Each bottom-bar destination keeps its own back stack (saved and restored on
 * tab switches); Home stays at the bottom, so Back from any tab returns Home before leaving the app.
 */
@Stable
class PortProofAppState(val navController: NavHostController) {

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    /** Non-null while a bottom-bar destination is on screen; the bar is shown only then. */
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val destination = currentDestination ?: return null
            return TopLevelDestination.entries.firstOrNull { destination.hasRoute(it.route) }
        }

    fun navigateToTopLevel(destination: TopLevelDestination) {
        val options = navOptions {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        when (destination) {
            TopLevelDestination.HOME -> navController.navigate(HomeRoute, options)
            TopLevelDestination.PROJECTS -> navController.navigate(ProjectsRoute(), options)
            TopLevelDestination.SEARCH -> navController.navigate(SearchRoute, options)
            TopLevelDestination.OVERVIEW -> navController.navigate(OverviewRoute, options)
            TopLevelDestination.SETTINGS -> navController.navigate(SettingsRoute, options)
        }
    }

    /** Opens the Projects tab with a filter (and optionally a sort order) preselected. */
    fun openProjects(filter: ProjectListFilter, sort: ProjectSort? = null) {
        navController.navigate(ProjectsRoute(filter.code, sort?.code)) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
        }
    }

    /**
     * Pops only when the current entry is resumed, so a double tap on a back button during a
     * transition cannot pop two screens.
     */
    fun navigateBack() {
        if (isResumed()) navController.popBackStack()
    }

    /** Leaves the current flow and returns to Home, keeping Home's state. */
    fun navigateHome() {
        if (isResumed()) navController.popBackStack(HomeRoute, inclusive = false)
    }

    private fun isResumed() = navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
}
