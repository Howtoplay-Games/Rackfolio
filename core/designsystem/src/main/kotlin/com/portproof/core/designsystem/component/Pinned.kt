package com.portproof.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.theme.PortProofTheme

/**
 * White label pinned to a point by a thin stem, like the reference's "170 kcal" tags on the
 * welcome photo. The dot at the bottom marks the point; place the composable so the dot sits on it.
 */
@Composable
fun PpPinnedLabel(text: String, modifier: Modifier = Modifier, stem: Dp = 30.dp) {
    val colors = PortProofTheme.colors
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text,
            style = PortProofTheme.numbers.caption,
            color = Color(0xFF1E1E1F),
            maxLines = 1,
            modifier = Modifier
                .background(Color.White, PortProofTheme.shapes.pill)
                .border(1.dp, colors.border, PortProofTheme.shapes.pill)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
        Box(Modifier.width(1.5.dp).height(stem).background(Color.White))
        Box(
            Modifier
                .size(9.dp)
                .background(Color.White, PortProofTheme.shapes.pill)
                .border(1.5.dp, Color(0x331E1E1F), PortProofTheme.shapes.pill),
        )
    }
}
