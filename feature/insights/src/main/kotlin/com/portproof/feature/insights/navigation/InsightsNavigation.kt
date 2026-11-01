package com.portproof.feature.insights.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.feature.insights.ActivityLogRoute as ActivityLogScreenRoute
import com.portproof.feature.insights.OverviewActions
import com.portproof.feature.insights.OverviewRoute as OverviewScreenRoute
import kotlinx.serialization.Serializable

/** "Project overview", opened from the black button on Home. Grows into Reports in Phase 4. */
@Serializable
data object OverviewRoute

/** Full activity log, opened from the overview's "See all". */
@Serializable
data object ActivityLogRoute

fun NavController.navigateToOverview() = navigate(OverviewRoute) { launchSingleTop = true }

fun NavController.navigateToActivityLog() = navigate(ActivityLogRoute) { launchSingleTop = true }

fun NavGraphBuilder.overviewScreen(
    onBack: () -> Unit,
    onOpenProjects: (ProjectListFilter) -> Unit,
    onOpenProject: (ProjectId) -> Unit,
    onOpenActivity: () -> Unit,
) {
    composable<OverviewRoute> { OverviewScreenRoute(OverviewActions(onBack, onOpenProjects, onOpenProject, onOpenActivity)) }
}

fun NavGraphBuilder.activityLogScreen(onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit) {
    composable<ActivityLogRoute> { ActivityLogScreenRoute(onBack = onBack, onOpenProject = onOpenProject) }
}
