package com.portproof.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.core.model.ProjectListFilter
import com.portproof.feature.dashboard.DashboardActions
import com.portproof.feature.dashboard.DashboardRoute
import kotlinx.serialization.Serializable

/** Start destination and navigation hub of the app. */
@Serializable
data object HomeRoute

fun NavGraphBuilder.homeScreen(
    onOpenOverview: () -> Unit,
    onCreateProject: () -> Unit,
    onOpenProjects: (ProjectListFilter) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    composable<HomeRoute> {
        DashboardRoute(DashboardActions(onOpenOverview, onCreateProject, onOpenProjects, onOpenSettings, onOpenSearch))
    }
}
