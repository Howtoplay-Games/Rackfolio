package com.portproof.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.core.model.ProjectSort
import com.portproof.feature.dashboard.navigation.HomeRoute
import com.portproof.feature.dashboard.navigation.homeScreen
import com.portproof.feature.insights.navigation.activityLogScreen
import com.portproof.feature.insights.navigation.navigateToActivityLog
import com.portproof.feature.insights.navigation.overviewScreen
import com.portproof.feature.login.navigation.navigateToSecurity
import com.portproof.feature.login.navigation.securityScreen
import com.portproof.feature.projects.navigation.ProjectEditorRoute
import com.portproof.feature.projects.navigation.navigateToProjectDetail
import com.portproof.feature.projects.navigation.navigateToProjectEditor
import com.portproof.feature.projects.navigation.projectDetailScreen
import com.portproof.feature.projects.navigation.projectEditorScreen
import com.portproof.feature.projects.navigation.projectsScreen
import com.portproof.feature.search.navigation.searchScreen
import com.portproof.feature.settings.navigation.settingsScreen
import com.portproof.feature.subscription.navigation.navigateToPro
import com.portproof.feature.subscription.navigation.proScreen

/**
 * App navigation graph. Feature modules expose `NavGraphBuilder` extensions and receive navigation
 * as callbacks, so features never depend on each other.
 */
@Composable
fun PortProofNavHost(appState: PortProofAppState, onProjectDeleted: () -> Unit, modifier: Modifier = Modifier) {
    val navController = appState.navController
    val openProject = { id: ProjectId -> navController.navigateToProjectDetail(id) }
    val editProject = { id: ProjectId -> navController.navigateToProjectEditor(id) }
    val createProject = { navController.navigateToProjectEditor() }
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
            onOpenSchedule = { appState.openProjects(ProjectListFilter.ACTIVE, ProjectSort.START_DATE) },
            onOpenActivity = navController::navigateToActivityLog,
            onCreateProject = createProject,
            onOpenProject = openProject,
            onEditProject = editProject,
        )
        projectsScreen(onProjectClick = openProject, onEditProject = editProject, onCreateProject = createProject)
        searchScreen(onOpenProject = openProject)
        overviewScreen(
            onOpenProjects = { appState.openProjects(it) },
            onOpenProject = openProject,
            onOpenActivity = navController::navigateToActivityLog,
        )
        settingsScreen(onOpenPro = navController::navigateToPro, onOpenSecurity = navController::navigateToSecurity)
        activityLogScreen(onBack = appState::navigateBack, onOpenProject = openProject)
        securityScreen(onBack = appState::navigateBack)
        proScreen(onClose = appState::navigateBack)
        projectDetailScreen(
            onBack = appState::navigateBack,
            onHome = appState::navigateHome,
            onEdit = editProject,
            onDeleted = {
                navController.popBackStack()
                onProjectDeleted()
            },
            onOpenPro = navController::navigateToPro,
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
            onOpenPro = navController::navigateToPro,
        )
    }
}
