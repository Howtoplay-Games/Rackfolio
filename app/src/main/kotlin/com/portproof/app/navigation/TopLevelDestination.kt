package com.portproof.app.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.portproof.app.R
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.feature.dashboard.navigation.HomeRoute
import com.portproof.feature.projects.navigation.ProjectsRoute
import com.portproof.feature.settings.navigation.SettingsRoute
import kotlin.reflect.KClass

/**
 * Destinations shown in the bottom bar / navigation rail. Equipment (Phase 2) and Reports
 * (Phase 4) are added here when those features exist, so the bar never offers an empty tab.
 */
enum class TopLevelDestination(
    val route: KClass<*>,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
) {
    HOME(HomeRoute::class, PortProofIcons.Home, R.string.nav_home),
    PROJECTS(ProjectsRoute::class, PortProofIcons.Projects, R.string.nav_projects),
    SETTINGS(SettingsRoute::class, PortProofIcons.Settings, R.string.nav_settings),
}
