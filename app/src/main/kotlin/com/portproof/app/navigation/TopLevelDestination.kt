package com.portproof.app.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.portproof.app.R
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.feature.dashboard.navigation.HomeRoute
import com.portproof.feature.insights.navigation.MetricsRoute
import com.portproof.feature.projects.navigation.ProjectsRoute
import com.portproof.feature.search.navigation.SearchRoute
import com.portproof.feature.settings.navigation.SettingsRoute
import kotlin.reflect.KClass

/**
 * The five destinations of the bottom bar / navigation rail, in the reference design's order.
 * Phase 2 turns SEARCH into Equipment (search moves into the lists) and Phase 4 extends INSIGHTS
 * into Reports, matching the planned Home / Projects / Equipment / Reports / Settings layout.
 */
enum class TopLevelDestination(
    val route: KClass<*>,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
) {
    HOME(HomeRoute::class, PortProofIcons.Home, R.string.nav_home),
    PROJECTS(ProjectsRoute::class, PortProofIcons.Projects, R.string.nav_projects),
    SEARCH(SearchRoute::class, PortProofIcons.Search, R.string.nav_search),
    INSIGHTS(MetricsRoute::class, PortProofIcons.BarChart, R.string.nav_insights),
    SETTINGS(SettingsRoute::class, PortProofIcons.Settings, R.string.nav_settings),
}
