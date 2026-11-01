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

/** Light weights and generous sizes, as in the reference: titles are Medium, figures Regular. */
internal val PortProofTypography = Typography(
    displaySmall = style(32, FontWeight.Normal, 38, -0.02),
    headlineMedium = style(26, FontWeight.Medium, 32, -0.02),
    headlineSmall = style(22, FontWeight.Normal, 28, -0.015),
    titleLarge = style(20, FontWeight.Medium, 26, -0.01),
    titleMedium = style(18, FontWeight.Medium, 24, -0.01),
    titleSmall = style(15, FontWeight.Medium, 20, -0.005),
    bodyLarge = style(16, FontWeight.Normal, 24),
    bodyMedium = style(14, FontWeight.Normal, 20),
    bodySmall = style(12, FontWeight.Normal, 16, 0.005),
    labelLarge = style(14, FontWeight.Medium, 20),
    labelMedium = style(12, FontWeight.Medium, 16, 0.005),
    labelSmall = style(11, FontWeight.Normal, 14, 0.01),
)

/** Figure styles. Tabular numerals keep changing values from shifting horizontally. */
@Immutable
data class PortProofNumberStyles(
    val metricLarge: TextStyle = style(32, FontWeight.Normal, 38, -0.02).copy(fontFeatureSettings = "tnum"),
    val metric: TextStyle = style(24, FontWeight.Normal, 30, -0.02).copy(fontFeatureSettings = "tnum"),
    val metricUnit: TextStyle = style(16, FontWeight.Normal, 22),
    val metricSmall: TextStyle = style(15, FontWeight.Medium, 20, -0.01).copy(fontFeatureSettings = "tnum"),
    val caption: TextStyle = style(11, FontWeight.Medium, 14, 0.01).copy(fontFeatureSettings = "tnum"),
)
