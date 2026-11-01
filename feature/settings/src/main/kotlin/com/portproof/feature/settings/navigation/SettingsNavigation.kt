package com.portproof.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.feature.settings.SettingsRoute as SettingsScreenRoute
import kotlinx.serialization.Serializable

/** Settings, opened from the avatar on Home. */
@Serializable
data object SettingsRoute

fun NavController.navigateToSettings() = navigate(SettingsRoute) { launchSingleTop = true }

fun NavGraphBuilder.settingsScreen(onBack: () -> Unit) {
    composable<SettingsRoute> { SettingsScreenRoute(onBack = onBack) }
}
