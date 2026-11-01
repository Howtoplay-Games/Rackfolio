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

/** Reference type scale: bold, tightly tracked titles; small regular captions. */
internal val PortProofTypography = Typography(
    displaySmall = style(32, FontWeight.Bold, 38, -0.03),
    headlineMedium = style(30, FontWeight.Bold, 36, -0.03),
    headlineSmall = style(24, FontWeight.Bold, 30, -0.025),
    titleLarge = style(20, FontWeight.SemiBold, 26, -0.015),
    titleMedium = style(17, FontWeight.SemiBold, 22, -0.01),
    titleSmall = style(15, FontWeight.SemiBold, 20, -0.005),
    bodyLarge = style(16, FontWeight.Normal, 24),
    bodyMedium = style(14, FontWeight.Normal, 20),
    bodySmall = style(12, FontWeight.Normal, 16, 0.005),
    labelLarge = style(14, FontWeight.SemiBold, 20),
    labelMedium = style(12, FontWeight.Medium, 16, 0.005),
    labelSmall = style(11, FontWeight.Normal, 14, 0.01),
)

/** Figure styles. Tabular numerals keep changing values from shifting horizontally. */
@Immutable
data class PortProofNumberStyles(
    /** Gauge centre ("1250 kcal" in the reference). */
    val metricLarge: TextStyle = style(30, FontWeight.Bold, 36, -0.03).copy(fontFeatureSettings = "tnum"),
    /** Card figures ("693 kcal"). */
    val metric: TextStyle = style(22, FontWeight.Bold, 28, -0.02).copy(fontFeatureSettings = "tnum"),
    val metricUnit: TextStyle = style(13, FontWeight.Medium, 18),
    /** Small figures under card titles ("48g"). */
    val metricSmall: TextStyle = style(13, FontWeight.SemiBold, 18).copy(fontFeatureSettings = "tnum"),
    val caption: TextStyle = style(11, FontWeight.Medium, 14, 0.01).copy(fontFeatureSettings = "tnum"),
)
