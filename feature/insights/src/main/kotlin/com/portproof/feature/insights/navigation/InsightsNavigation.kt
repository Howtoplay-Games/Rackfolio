package com.portproof.feature.insights.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.core.model.ProjectId
import com.portproof.core.model.ProjectListFilter
import com.portproof.feature.insights.ActivityLogRoute as ActivityLogScreenRoute
import com.portproof.feature.insights.InsightRoute as InsightScreenRoute
import com.portproof.feature.insights.MetricsRoute as MetricsScreenRoute
import kotlinx.serialization.Serializable

/** Top-level "Insights" tab (reference "Metrics Summary"). Becomes part of Reports in Phase 4. */
@Serializable
data object MetricsRoute

/** Detail screen behind the dashboard's "See Details" (reference "Insight"). */
@Serializable
data object InsightRoute

/** Full activity log behind the dashboard bell. */
@Serializable
data object ActivityLogRoute

fun NavController.navigateToInsight() = navigate(InsightRoute) { launchSingleTop = true }

fun NavController.navigateToActivityLog() = navigate(ActivityLogRoute) { launchSingleTop = true }

fun NavGraphBuilder.metricsScreen(onOpenProjects: (ProjectListFilter) -> Unit) {
    composable<MetricsRoute> { MetricsScreenRoute(onOpenProjects = onOpenProjects) }
}

fun NavGraphBuilder.insightScreen(onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit) {
    composable<InsightRoute> { InsightScreenRoute(onBack = onBack, onOpenProject = onOpenProject) }
}

fun NavGraphBuilder.activityLogScreen(onBack: () -> Unit, onOpenProject: (ProjectId) -> Unit) {
    composable<ActivityLogRoute> { ActivityLogScreenRoute(onBack = onBack, onOpenProject = onOpenProject) }
}
