package com.portproof.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Space that top-level screens must leave at the bottom of scrolling content so the floating
 * navigation bar never covers it. Provided by the app shell; 0dp when no bar is shown.
 */
val LocalBottomBarInset = staticCompositionLocalOf<Dp> { 0.dp }
