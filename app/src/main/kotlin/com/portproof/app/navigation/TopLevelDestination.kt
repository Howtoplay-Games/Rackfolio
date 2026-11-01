package com.portproof.app.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.portproof.app.R
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.feature.dashboard.navigation.HomeRoute
import com.portproof.feature.insights.navigation.OverviewRoute
import com.portproof.feature.projects.navigation.ProjectsRoute
import com.portproof.feature.search.navigation.SearchRoute
import com.portproof.feature.settings.navigation.SettingsRoute
import kotlin.reflect.KClass

/**
 * The five slots of the bottom bar, in the reference's order. [center] marks the orange square in
 * the middle (a scan button in the reference; search here until QR scanning arrives in Phase 2).
 */
enum class TopLevelDestination(
    val route: KClass<*>,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
    val center: Boolean = false,
) {
    HOME(HomeRoute::class, PortProofIcons.Home, R.string.nav_home),
    PROJECTS(ProjectsRoute::class, PortProofIcons.BookOpen, R.string.nav_projects),
    SEARCH(SearchRoute::class, PortProofIcons.ScanSearch, R.string.nav_search, center = true),
    OVERVIEW(OverviewRoute::class, PortProofIcons.PieChart, R.string.nav_overview),
    SETTINGS(SettingsRoute::class, PortProofIcons.Settings, R.string.nav_settings),
}
