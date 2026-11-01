package com.portproof.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * PortProof colour tokens. Light theme: cool grey canvas, white cards, near-black text and a
 * restrained indigo-blue accent. Dark theme: navy/graphite foundations.
 * Every text/background pair used for content meets WCAG AA (4.5:1); see ARCHITECTURE.md.
 */
@Immutable
data class PortProofColors(
    val background: Color,
    val surface: Color,
    /** Icon tiles, unselected navigation items, input backgrounds. */
    val surfaceMuted: Color,
    /** Segmented-control track, gauge and chart tracks. */
    val surfaceSunken: Color,
    val border: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    /** Emphasis surfaces such as the selected navigation item (near-black in light theme). */
    val inverseSurface: Color,
    val onInverseSurface: Color,
    val success: Color,
    val successText: Color,
    val successContainer: Color,
    val warning: Color,
    val warningText: Color,
    val warningContainer: Color,
    val danger: Color,
    val dangerText: Color,
    val dangerContainer: Color,
    val chartSecondary: Color,
    val scrim: Color,
    val isDark: Boolean,
)

internal val LightPortProofColors = PortProofColors(
    background = Color(0xFFF2F3F6),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFF5F6F8),
    surfaceSunken = Color(0xFFEBEDF1),
    border = Color(0xFFE3E6EB),
    divider = Color(0xFFECEEF2),
    textPrimary = Color(0xFF0E1116),
    textSecondary = Color(0xFF5B6270),
    textMuted = Color(0xFF636A78),
    accent = Color(0xFF4453D6),
    onAccent = Color(0xFFFFFFFF),
    accentContainer = Color(0xFFE7E9FC),
    onAccentContainer = Color(0xFF2F3BB0),
    inverseSurface = Color(0xFF12151B),
    onInverseSurface = Color(0xFFFFFFFF),
    success = Color(0xFF16A34A),
    successText = Color(0xFF146C34),
    successContainer = Color(0xFFE3F7EA),
    warning = Color(0xFFD97706),
    warningText = Color(0xFF9A5B05),
    warningContainer = Color(0xFFFEF3DC),
    danger = Color(0xFFE5484D),
    dangerText = Color(0xFFC8291C),
    dangerContainer = Color(0xFFFDECEA),
    chartSecondary = Color(0xFF14B8A6),
    scrim = Color(0x66000000),
    isDark = false,
)

internal val DarkPortProofColors = PortProofColors(
    background = Color(0xFF0A0E15),
    surface = Color(0xFF121722),
    surfaceMuted = Color(0xFF1A202C),
    surfaceSunken = Color(0xFF202838),
    border = Color(0xFF273042),
    divider = Color(0xFF1E2633),
    textPrimary = Color(0xFFE6E9EF),
    textSecondary = Color(0xFFA3ABBA),
    textMuted = Color(0xFF8C94A4),
    accent = Color(0xFF93A0FF),
    onAccent = Color(0xFF0A0E15),
    accentContainer = Color(0xFF232A57),
    onAccentContainer = Color(0xFFDCE0FF),
    inverseSurface = Color(0xFFE6E9EF),
    onInverseSurface = Color(0xFF0A0E15),
    success = Color(0xFF22C55E),
    successText = Color(0xFF4ADE80),
    successContainer = Color(0xFF10261A),
    warning = Color(0xFFF59E0B),
    warningText = Color(0xFFFBBF24),
    warningContainer = Color(0xFF33260A),
    danger = Color(0xFFF26B6B),
    dangerText = Color(0xFFF87171),
    dangerContainer = Color(0xFF3A1515),
    chartSecondary = Color(0xFF2DD4BF),
    scrim = Color(0x99000000),
    isDark = true,
)
