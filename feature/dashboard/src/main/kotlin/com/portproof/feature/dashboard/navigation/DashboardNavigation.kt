package com.portproof.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.core.model.ProjectId
import com.portproof.feature.dashboard.DashboardActions
import com.portproof.feature.dashboard.DashboardRoute
import kotlinx.serialization.Serializable

/** Start destination: the dashboard tab. */
@Serializable
data object HomeRoute

fun NavGraphBuilder.homeScreen(
    onOpenSchedule: () -> Unit,
    onOpenActivity: () -> Unit,
    onCreateProject: () -> Unit,
    onOpenProject: (ProjectId) -> Unit,
    onEditProject: (ProjectId) -> Unit,
) {
    composable<HomeRoute> {
        DashboardRoute(DashboardActions(onOpenSchedule, onOpenActivity, onCreateProject, onOpenProject, onEditProject))
    }
}
