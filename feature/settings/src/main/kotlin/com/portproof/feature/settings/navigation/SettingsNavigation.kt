package com.portproof.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.feature.settings.SettingsRoute as SettingsScreenRoute
import kotlinx.serialization.Serializable

/** Top-level "Settings" tab. */
@Serializable
data object SettingsRoute

fun NavGraphBuilder.settingsScreen() {
    composable<SettingsRoute> { SettingsScreenRoute() }
}
