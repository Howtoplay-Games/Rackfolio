package com.portproof.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.portproof.feature.dashboard.navigation.HomeRoute
import com.portproof.feature.dashboard.navigation.homeScreen
import com.portproof.feature.insights.navigation.activityLogScreen
import com.portproof.feature.insights.navigation.navigateToActivityLog
import com.portproof.feature.insights.navigation.navigateToOverview
import com.portproof.feature.insights.navigation.overviewScreen
import com.portproof.feature.projects.navigation.ProjectEditorRoute
import com.portproof.feature.projects.navigation.navigateToProjectDetail
import com.portproof.feature.projects.navigation.navigateToProjectEditor
import com.portproof.feature.projects.navigation.navigateToProjects
import com.portproof.feature.projects.navigation.projectDetailScreen
import com.portproof.feature.projects.navigation.projectEditorScreen
import com.portproof.feature.projects.navigation.projectsScreen
import com.portproof.feature.search.navigation.navigateToSearch
import com.portproof.feature.search.navigation.searchScreen
import com.portproof.feature.settings.navigation.navigateToSettings
import com.portproof.feature.settings.navigation.settingsScreen

/**
 * App navigation graph. Feature modules expose `NavGraphBuilder` extensions and receive navigation
 * as callbacks, so features never depend on each other.
 */
@Composable
fun PortProofNavHost(appState: PortProofAppState, onProjectDeleted: () -> Unit, modifier: Modifier = Modifier) {
    val navController = appState.navController
    val openProject = { id: com.portproof.core.model.ProjectId -> navController.navigateToProjectDetail(id) }
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
            onOpenOverview = navController::navigateToOverview,
            onCreateProject = { navController.navigateToProjectEditor() },
            onOpenProjects = navController::navigateToProjects,
            onOpenSettings = navController::navigateToSettings,
            onOpenSearch = navController::navigateToSearch,
        )
        projectsScreen(onBack = appState::navigateBack, onProjectClick = openProject, onCreateProject = { navController.navigateToProjectEditor() })
        searchScreen(onBack = appState::navigateBack, onOpenProject = openProject)
        overviewScreen(
            onBack = appState::navigateBack,
            onOpenProjects = navController::navigateToProjects,
            onOpenProject = openProject,
            onOpenActivity = navController::navigateToActivityLog,
        )
        activityLogScreen(onBack = appState::navigateBack, onOpenProject = openProject)
        settingsScreen(onBack = appState::navigateBack)
        projectDetailScreen(
            onBack = appState::navigateBack,
            onHome = appState::navigateHome,
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
