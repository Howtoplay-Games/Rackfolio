package com.portproof.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.portproof.feature.settings.SettingsRoute as SettingsScreenRoute
import kotlinx.serialization.Serializable

/** Settings tab: plan, profile, login and security, appearance, privacy. */
@Serializable
data object SettingsRoute

fun NavGraphBuilder.settingsScreen(onOpenPro: () -> Unit, onOpenSecurity: () -> Unit) {
    composable<SettingsRoute> { SettingsScreenRoute(onOpenPro = onOpenPro, onOpenSecurity = onOpenSecurity) }
}
