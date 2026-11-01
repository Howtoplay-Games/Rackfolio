package com.portproof.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/** Inter Bold "P" outline in a 108-unit box, shared with the launcher icon (ic_launcher_foreground.xml). */
internal const val LOGO_P_PATH =
    "M41.29 71V37H54.55Q58.43 37 61.14 38.47Q63.86 39.94 65.28 42.53Q66.71 45.12 66.71 48.48Q66.71 51.86 65.26 54.42" +
        "Q63.81 56.99 61.05 58.44Q58.29 59.89 54.39 59.89H48.25V71ZM48.25 54.27H53.27Q56.46 54.27 57.99 52.65" +
        "Q59.52 51.03 59.52 48.48Q59.52 45.9 57.99 44.32Q56.46 42.75 53.25 42.75H48.25Z"

/** PortProof mark: a "P" inside four rounded scan-frame corners, as in the reference logo. */
@Composable
fun PpBracketLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = PortProofTheme.colors.logo,
    contentDescription: String? = null,
) {
    val letter = remember { PathParser().parsePathString(LOGO_P_PATH).toPath() }
    Canvas(
        modifier
            .size(size)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
    ) {
        scale(this.size.width / 108f, pivot = Offset.Zero) {
            drawPath(bracketPath(), color, style = Stroke(width = 7f, cap = StrokeCap.Round))
            drawPath(letter, color)
        }
    }
}

/** Four corner brackets with rounded joins, inset 18 units in the 108-unit box. */
internal fun bracketPath(): Path = Path().apply {
    val r = 10f
    val arm = 16f
    val a = 22f
    val b = 86f
    moveTo(a, a + r + arm); lineTo(a, a + r); arcTo(Rect(a, a, a + 2 * r, a + 2 * r), 180f, 90f, false); lineTo(a + r + arm, a)
    moveTo(b - r - arm, a); lineTo(b - r, a); arcTo(Rect(b - 2 * r, a, b, a + 2 * r), 270f, 90f, false); lineTo(b, a + r + arm)
    moveTo(b, b - r - arm); lineTo(b, b - r); arcTo(Rect(b - 2 * r, b - 2 * r, b, b), 0f, 90f, false); lineTo(b - r - arm, b)
    moveTo(a + r + arm, b); lineTo(a + r, b); arcTo(Rect(a, b - 2 * r, a + 2 * r, b), 90f, 90f, false); lineTo(a, b - r - arm)
}
