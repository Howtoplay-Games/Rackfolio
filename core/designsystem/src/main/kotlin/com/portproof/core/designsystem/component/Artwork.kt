package com.portproof.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.R
import com.portproof.core.designsystem.theme.PortProofTheme

/**
 * Bundled Fluent 3D emoji (MIT licence, see THIRD_PARTY_NOTICES.md). Bundling keeps the artwork
 * identical across devices instead of depending on each vendor's emoji font.
 */
enum class PpEmoji(@DrawableRes val res: Int) {
    Tools(R.drawable.emoji_tools),
    Calendar(R.drawable.emoji_calendar),
    Check(R.drawable.emoji_check),
    Pause(R.drawable.emoji_pause),
    Archive(R.drawable.emoji_archive),
    Folder(R.drawable.emoji_folder),
    Chart(R.drawable.emoji_chart),
    Hourglass(R.drawable.emoji_hourglass),
    BarChart(R.drawable.emoji_bar_chart),
    Memo(R.drawable.emoji_memo),
}

/** Larger artwork for feature cards, chosen per installation type. */
enum class PpIllustration(@DrawableRes val res: Int) {
    Camera(R.drawable.illus_camera),
    Computer(R.drawable.illus_computer),
    Key(R.drawable.illus_key),
    Speaker(R.drawable.illus_speaker),
    Television(R.drawable.illus_tv),
    Plug(R.drawable.illus_plug),
    Antenna(R.drawable.illus_antenna),
    Toolbox(R.drawable.illus_toolbox),
    Construction(R.drawable.illus_construction),
}

/** Emoji on a soft circle, as in the reference metric cards. Decorative: no content description. */
@Composable
fun PpEmojiBadge(
    emoji: PpEmoji,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    containerColor: Color = PortProofTheme.colors.surfaceMuted,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, PortProofTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(emoji.res), contentDescription = null, modifier = Modifier.size(size * 0.55f))
    }
}
