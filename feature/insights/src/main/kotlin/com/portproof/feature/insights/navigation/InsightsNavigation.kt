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

/** Overview tab: project counts, deadlines and recent activity. Grows into Reports in Phase 4. */
@Serializable
data object OverviewRoute

/** Full activity log, opened from the bell on Home and the overview's "See all". */
@Serializable
data object ActivityLogRoute

fun NavController.navigateToActivityLog() = navigate(ActivityLogRoute) { launchSingleTop = true }

fun NavGraphBuilder.overviewScreen(
    onOpenProjects: (ProjectListFilter) -> Unit,
    onOpenProject: (ProjectId) -> Unit,
    onOpenActivity: () -> Unit,
) {
    composable<OverviewRoute> { OverviewScreenRoute(OverviewActions(onOpenProjects, onOpenProject, onOpenActivity)) }
}

fun NavGraphBuilder.activityLogScreen(onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit) {
    composable<ActivityLogRoute> { ActivityLogScreenRoute(onBack = onBack, onOpenProject = onOpenProject) }
}
