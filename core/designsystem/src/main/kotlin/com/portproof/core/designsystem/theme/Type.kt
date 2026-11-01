package com.portproof.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.portproof.core.designsystem.R

/** Inter (SIL Open Font License 1.1), bundled so typography is identical offline on every device. */
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
)

internal val PortProofTypography = Typography(
    displaySmall = style(32, FontWeight.Bold, 38, -0.02),
    headlineMedium = style(26, FontWeight.SemiBold, 32, -0.02),
    headlineSmall = style(22, FontWeight.SemiBold, 28, -0.015),
    titleLarge = style(19, FontWeight.SemiBold, 26, -0.01),
    titleMedium = style(16, FontWeight.SemiBold, 22, -0.01),
    titleSmall = style(14, FontWeight.SemiBold, 20, -0.005),
    bodyLarge = style(16, FontWeight.Normal, 24),
    bodyMedium = style(14, FontWeight.Normal, 20),
    bodySmall = style(12, FontWeight.Normal, 16, 0.005),
    labelLarge = style(14, FontWeight.Medium, 20),
    labelMedium = style(12, FontWeight.Medium, 16, 0.005),
    labelSmall = style(11, FontWeight.Medium, 14, 0.01),
)

/** Extra styles for figures. Tabular numerals keep columns of numbers aligned. */
@Immutable
data class PortProofNumberStyles(
    val metricLarge: TextStyle = style(30, FontWeight.Bold, 36, -0.02).copy(fontFeatureSettings = "tnum"),
    val metric: TextStyle = style(22, FontWeight.Bold, 28, -0.02).copy(fontFeatureSettings = "tnum"),
    val metricSmall: TextStyle = style(15, FontWeight.SemiBold, 20, -0.01).copy(fontFeatureSettings = "tnum"),
    val caption: TextStyle = style(11, FontWeight.Medium, 14, 0.01).copy(fontFeatureSettings = "tnum"),
)
