package com.portproof.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.portproof.feature.dashboard.navigation.HomeRoute
import com.portproof.feature.dashboard.navigation.homeScreen
import com.portproof.feature.insights.navigation.activityLogScreen
import com.portproof.feature.insights.navigation.insightScreen
import com.portproof.feature.insights.navigation.metricsScreen
import com.portproof.feature.insights.navigation.navigateToActivityLog
import com.portproof.feature.insights.navigation.navigateToInsight
import com.portproof.feature.projects.navigation.ProjectEditorRoute
import com.portproof.feature.projects.navigation.navigateToProjectDetail
import com.portproof.feature.projects.navigation.navigateToProjectEditor
import com.portproof.feature.projects.navigation.projectDetailScreen
import com.portproof.feature.projects.navigation.projectEditorScreen
import com.portproof.feature.projects.navigation.projectsScreen
import com.portproof.feature.search.navigation.searchScreen
import com.portproof.feature.settings.navigation.settingsScreen

/**
 * App navigation graph. Feature modules expose `NavGraphBuilder` extensions and receive navigation
 * as callbacks, so features never depend on each other.
 */
@Composable
fun PortProofNavHost(appState: PortProofAppState, onProjectDeleted: () -> Unit, modifier: Modifier = Modifier) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = { fadeIn(tween(durationMillis = 220)) },
        exitTransition = { fadeOut(tween(durationMillis = 160)) },
        popEnterTransition = { fadeIn(tween(durationMillis = 220)) },
        popExitTransition = { fadeOut(tween(durationMillis = 160)) },
    ) {
        homeScreen(
            onOpenActivity = navController::navigateToActivityLog,
            onCreateProject = { navController.navigateToProjectEditor() },
            onOpenProject = { navController.navigateToProjectDetail(it) },
            onOpenProjects = appState::openProjects,
            onOpenSettings = { appState.navigateToTopLevel(TopLevelDestination.SETTINGS) },
            onOpenInsight = navController::navigateToInsight,
        )
        projectsScreen(
            onProjectClick = { navController.navigateToProjectDetail(it) },
            onCreateProject = { navController.navigateToProjectEditor() },
        )
        searchScreen(onOpenProject = { navController.navigateToProjectDetail(it) })
        metricsScreen(onOpenProjects = appState::openProjects)
        settingsScreen()
        insightScreen(onBack = appState::navigateBack, onOpenProject = { navController.navigateToProjectDetail(it) })
        activityLogScreen(onBack = appState::navigateBack, onOpenProject = { navController.navigateToProjectDetail(it) })
        projectDetailScreen(
            onBack = appState::navigateBack,
            onEdit = { navController.navigateToProjectEditor(it) },
            onDeleted = {
                navController.popBackStack()
                onProjectDeleted()
            },
        )
        projectEditorScreen(
            onClose = appState::navigateBack,
            onSaved = { id, created ->
                if (created) {
                    // Replace the editor with the new project's detail screen.
                    navController.navigateToProjectDetail(id, navOptions { popUpTo<ProjectEditorRoute> { inclusive = true } })
                } else {
                    navController.popBackStack()
                }
            },
        )
    }
}
