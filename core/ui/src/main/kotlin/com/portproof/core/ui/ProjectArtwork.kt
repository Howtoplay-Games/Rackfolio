package com.portproof.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.portproof.core.designsystem.component.PpIllustration
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.Project
import java.time.LocalTime

private val artworkRules: List<Pair<Regex, PpIllustration>> = listOf(
    "cctv|camera|cameras|anpr|surveillance|nvr|dvr|vms" to PpIllustration.Camera,
    "access|door|doors|reader|readers|intercom|lock|locks|entry|turnstile" to PpIllustration.Key,
    "audio|speaker|speakers|pa|sound|paging" to PpIllustration.Speaker,
    "av|display|displays|screen|screens|tv|projector|signage|video ?wall" to PpIllustration.Television,
    "wifi|wi-fi|wireless|antenna|radio|das|5g|lte" to PpIllustration.Antenna,
    "network|networking|cabling|data|switch|switches|server|rack|fibre|fiber|lan|it" to PpIllustration.Computer,
    "power|electrical|ups|socket|sockets|ev|charger" to PpIllustration.Plug,
).map { (words, art) -> Regex("\\b($words)\\b", RegexOption.IGNORE_CASE) to art }

/** Artwork that matches the kind of installation, based on words in the name and description. */
fun Project.illustration(): PpIllustration {
    val text = listOfNotNull(name, description).joinToString(" ")
    return artworkRules.firstOrNull { (regex, _) -> regex.containsMatchIn(text) }?.second ?: PpIllustration.Toolbox
}

/** Pastel colour for the n-th feature card, cycling lavender, lime, sky, peach. */
@Composable
fun pastelFor(index: Int): Color {
    val pastels = PortProofTheme.colors.pastels
    return pastels[index.mod(pastels.size)]
}

@StringRes
fun greetingRes(time: LocalTime = LocalTime.now()): Int = when (time.hour) {
    in 5..11 -> R.string.ui_greeting_morning
    in 12..17 -> R.string.ui_greeting_afternoon
    else -> R.string.ui_greeting_evening
}
