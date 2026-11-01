package com.portproof.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 4dp-based spacing scale. */
@Immutable
data class PortProofSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    /** Horizontal padding of screen content on phones. */
    val screenGutter: Dp = 16.dp,
    /** Gap between stacked cards. */
    val cardGap: Dp = 14.dp,
    /** Gap between screen sections (title + content blocks). */
    val sectionGap: Dp = 24.dp,
    /** Readable maximum width for content on tablets and foldables. */
    val maxContentWidth: Dp = 880.dp,
)

/** Corner radii taken from the reference: 24dp glass tiles, 20dp document cards, pill controls. */
@Immutable
data class PortProofShapes(
    val tile: RoundedCornerShape = RoundedCornerShape(24.dp),
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val panel: RoundedCornerShape = RoundedCornerShape(16.dp),
    val sheet: RoundedCornerShape = RoundedCornerShape(28.dp),
    val header: RoundedCornerShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
    val highlight: RoundedCornerShape = RoundedCornerShape(4.dp),
    val control: RoundedCornerShape = RoundedCornerShape(percent = 50),
    val input: RoundedCornerShape = RoundedCornerShape(percent = 50),
    val field: RoundedCornerShape = RoundedCornerShape(16.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(percent = 50),
)

internal val PortProofMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
