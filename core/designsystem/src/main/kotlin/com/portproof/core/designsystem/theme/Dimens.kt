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

/** Corner radii taken from the reference: soft 20dp cards, pill buttons and navigation. */
@Immutable
data class PortProofShapes(
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val hero: RoundedCornerShape = RoundedCornerShape(22.dp),
    val feature: RoundedCornerShape = RoundedCornerShape(24.dp),
    val panel: RoundedCornerShape = RoundedCornerShape(16.dp),
    val tile: RoundedCornerShape = RoundedCornerShape(12.dp),
    val control: RoundedCornerShape = RoundedCornerShape(percent = 50),
    val input: RoundedCornerShape = RoundedCornerShape(14.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(percent = 50),
    val sheet: RoundedCornerShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
)

internal val PortProofMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
