package com.portproof.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalPortProofColors = staticCompositionLocalOf { LightPortProofColors }
private val LocalPortProofSpacing = staticCompositionLocalOf { PortProofSpacing() }
private val LocalPortProofShapes = staticCompositionLocalOf { PortProofShapes() }
private val LocalPortProofNumbers = staticCompositionLocalOf { PortProofNumberStyles() }

/** Accessors for PortProof tokens, alongside [MaterialTheme] for Material components. */
object PortProofTheme {
    val colors: PortProofColors
        @Composable @ReadOnlyComposable get() = LocalPortProofColors.current
    val spacing: PortProofSpacing
        @Composable @ReadOnlyComposable get() = LocalPortProofSpacing.current
    val shapes: PortProofShapes
        @Composable @ReadOnlyComposable get() = LocalPortProofShapes.current
    val numbers: PortProofNumberStyles
        @Composable @ReadOnlyComposable get() = LocalPortProofNumbers.current
}

/** Dynamic colour is intentionally not used: the brand palette is part of the product identity. */
@Composable
fun PortProofTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkPortProofColors else LightPortProofColors
    CompositionLocalProvider(LocalPortProofColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = PortProofTypography,
            shapes = PortProofMaterialShapes,
            content = content,
        )
    }
}

/** Maps PortProof tokens onto Material 3 roles so stock components (dialogs, pickers) match. */
private fun PortProofColors.toMaterialColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accentContainer,
        onPrimaryContainer = onAccentContainer,
        secondary = textSecondary,
        onSecondary = surface,
        secondaryContainer = surfaceSunken,
        onSecondaryContainer = textPrimary,
        tertiary = chartPeak,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceMuted,
        onSurfaceVariant = textSecondary,
        surfaceTint = accent,
        surfaceBright = surface,
        surfaceDim = background,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerHighest = surfaceMuted,
        inverseSurface = inverseSurface,
        inverseOnSurface = onInverseSurface,
        inversePrimary = accentContainer,
        outline = border,
        outlineVariant = divider,
        error = dangerText,
        onError = surface,
        errorContainer = dangerContainer,
        onErrorContainer = dangerText,
        scrim = scrim,
    )
}
