package com.portproof.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.feature.dashboard.DashboardRoute
import kotlinx.serialization.Serializable

/** Start destination of the app ("Home" tab). */
@Serializable
data object HomeRoute

fun NavGraphBuilder.homeScreen(
    onSearch: () -> Unit,
    onCreateProject: () -> Unit,
    onOpenProject: (ProjectId) -> Unit,
    onOpenProjects: (ProjectListFilter) -> Unit,
    onOpenSettings: () -> Unit,
) {
    composable<HomeRoute> {
        DashboardRoute(
            onSearch = onSearch,
            onCreateProject = onCreateProject,
            onOpenProject = onOpenProject,
            onOpenProjects = onOpenProjects,
            onOpenSettings = onOpenSettings,
        )
    }
}
