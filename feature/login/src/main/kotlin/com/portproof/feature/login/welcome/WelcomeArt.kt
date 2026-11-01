package com.portproof.feature.login.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.component.PpPinnedLabel
import com.portproof.core.designsystem.theme.PortProofTheme
import kotlin.math.roundToInt

/** A label pinned at a point given as fractions of the artwork's width and height. */
internal data class ArtPin(val x: Float, val y: Float, val text: String)

/**
 * Welcome artwork: a drawing with pinned labels, standing in for the reference's food photo with
 * calorie tags. The dot under each label sits exactly on its point.
 */
@Composable
internal fun PinnedArt(description: String, pins: List<ArtPin>, modifier: Modifier = Modifier, draw: DrawScope.(ArtColors) -> Unit) {
    val colors = artColors()
    Layout(
        modifier = modifier.semantics { contentDescription = description },
        content = {
            Canvas(Modifier.fillMaxSize()) { draw(colors) }
            pins.forEach { PpPinnedLabel(it.text) }
        },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val canvas = measurables.first().measure(Constraints.fixed(width, height))
        val labels = measurables.drop(1).map { it.measure(Constraints()) }
        val dotRadius = 4.5.dp.toPx()
        layout(width, height) {
            canvas.place(0, 0)
            labels.forEachIndexed { i, label ->
                val pin = pins[i]
                val x = (pin.x * width - label.width / 2f).roundToInt().coerceIn(0, (width - label.width).coerceAtLeast(0))
                val y = (pin.y * height - label.height + dotRadius).roundToInt()
                label.place(x, y)
            }
        }
    }
}

internal data class ArtColors(
    val glow: Color,
    val body: Color,
    val unit: Color,
    val port: Color,
    val accent: Color,
    val card: Color,
    val cell: Color,
    val red: Color,
    val yellow: Color,
    val green: Color,
    val ink: Color,
)

@Composable
private fun artColors(): ArtColors {
    val c = PortProofTheme.colors
    return ArtColors(
        glow = c.accentContainer,
        body = if (c.isDark) Color(0xFF3A3836) else Color(0xFF2B2B2E),
        unit = if (c.isDark) Color(0xFF4A4744) else Color(0xFF3C3C41),
        port = Color(0xFF17171A),
        accent = c.accent,
        card = c.surface,
        cell = c.surfaceMuted,
        red = Color(0xFFF08A95),
        yellow = Color(0xFFF2D35B),
        green = Color(0xFF34C77B),
        ink = c.textPrimary,
    )
}

private fun DrawScope.glow(colors: ArtColors) {
    drawCircle(
        Brush.radialGradient(listOf(colors.glow, Color.Transparent), center, size.minDimension * 0.62f),
        radius = size.minDimension * 0.62f,
    )
}

/** Page 1: a network cabinet with a patch panel, a PoE switch and an NVR. */
internal fun DrawScope.drawRack(colors: ArtColors) {
    glow(colors)
    val w = size.minDimension * 0.62f
    val h = size.height * 0.84f
    val left = (size.width - w) / 2f
    val top = (size.height - h) / 2f
    drawRoundRect(colors.body, Offset(left, top), Size(w, h), CornerRadius(w * 0.08f))
    val pad = w * 0.07f
    val unitH = (h - pad * 5) / 4f
    repeat(4) { u ->
        val y = top + pad + u * (unitH + pad)
        drawRoundRect(colors.unit, Offset(left + pad, y), Size(w - pad * 2, unitH), CornerRadius(unitH * 0.18f))
        when (u) {
            0, 1 -> {
                // Twelve ports; the switch (unit 1) shows link lights above them.
                val ports = 6
                val gap = (w - pad * 4) / (ports * 1.6f)
                val portSize = gap * 1.1f
                repeat(ports * 2) { p ->
                    val row = p / ports
                    val col = p % ports
                    val px = left + pad * 2 + col * gap * 1.6f
                    val py = y + unitH * 0.18f + row * (portSize + unitH * 0.12f)
                    drawRoundRect(colors.port, Offset(px, py), Size(portSize, portSize), CornerRadius(portSize * 0.2f))
                    if (u == 0 && p % 3 == 0) drawRect(colors.accent, Offset(px + portSize * 0.3f, py + portSize * 0.35f), Size(portSize * 0.4f, portSize * 0.65f))
                    if (u == 1 && p % 2 == 0) drawCircle(colors.green, portSize * 0.14f, Offset(px + portSize * 0.5f, py + portSize * 0.28f))
                }
            }
            2 -> repeat(4) { d ->
                val bayW = (w - pad * 2 - pad * 1.5f) / 4f - pad * 0.4f
                val bx = left + pad * 1.8f + d * (bayW + pad * 0.4f)
                drawRoundRect(colors.port, Offset(bx, y + unitH * 0.2f), Size(bayW, unitH * 0.6f), CornerRadius(unitH * 0.1f))
                drawCircle(colors.accent, unitH * 0.05f, Offset(bx + bayW * 0.82f, y + unitH * 0.32f))
            }
            else -> repeat(5) { v ->
                val vy = y + unitH * (0.22f + v * 0.14f)
                drawLine(colors.port, Offset(left + pad * 2, vy), Offset(left + w - pad * 2, vy), strokeWidth = unitH * 0.05f)
            }
        }
    }
}

/** Page 2: a month view with an in-progress run (orange), an overdue day (red) and a gap (yellow). */
internal fun DrawScope.drawSchedule(colors: ArtColors) {
    glow(colors)
    val w = size.minDimension * 0.8f
    val h = size.height * 0.72f
    val left = (size.width - w) / 2f
    val top = (size.height - h) / 2f
    drawRoundRect(colors.card, Offset(left, top), Size(w, h), CornerRadius(w * 0.07f))
    drawRoundRect(colors.body, Offset(left, top), Size(w, h * 0.16f), CornerRadius(w * 0.07f))
    drawRect(colors.body, Offset(left, top + h * 0.08f), Size(w, h * 0.08f))
    repeat(2) { r -> drawCircle(colors.card, h * 0.025f, Offset(left + w * (0.25f + r * 0.5f), top + h * 0.08f)) }
    val pad = w * 0.06f
    val cell = (w - pad * 2) / 7f
    repeat(28) { i ->
        val col = i % 7
        val row = i / 7
        val color = when (i) {
            in 8..19 -> colors.accent
            22 -> colors.red
            27 -> colors.yellow
            else -> colors.cell
        }
        drawRoundRect(
            color,
            Offset(left + pad + col * cell + cell * 0.1f, top + h * 0.22f + row * cell * 1.02f + cell * 0.1f),
            Size(cell * 0.8f, cell * 0.8f),
            CornerRadius(cell * 0.2f),
        )
    }
}

/** Page 3: a phone showing the PIN screen, padlock in orange. */
internal fun DrawScope.drawPhone(colors: ArtColors) {
    glow(colors)
    val h = size.height * 0.86f
    val w = h * 0.52f
    val left = (size.width - w) / 2f
    val top = (size.height - h) / 2f
    drawRoundRect(colors.body, Offset(left, top), Size(w, h), CornerRadius(w * 0.16f))
    val inset = w * 0.06f
    drawRoundRect(colors.card, Offset(left + inset, top + inset), Size(w - inset * 2, h - inset * 2), CornerRadius(w * 0.11f))
    drawRoundRect(colors.body, Offset(left + w * 0.38f, top + inset * 1.6f), Size(w * 0.24f, w * 0.05f), CornerRadius(w * 0.025f))
    // Padlock.
    val cx = left + w / 2f
    val lockW = w * 0.26f
    val lockTop = top + h * 0.2f
    drawArc(
        colors.accent, 180f, 180f, useCenter = false,
        topLeft = Offset(cx - lockW * 0.32f, lockTop), size = Size(lockW * 0.64f, lockW * 0.7f),
        style = Stroke(width = lockW * 0.12f),
    )
    drawRoundRect(colors.accent, Offset(cx - lockW / 2f, lockTop + lockW * 0.34f), Size(lockW, lockW * 0.78f), CornerRadius(lockW * 0.16f))
    // Four of six PIN dots filled.
    val dotY = top + h * 0.5f
    repeat(6) { d ->
        val dx = cx + (d - 2.5f) * w * 0.11f
        if (d < 4) drawCircle(colors.ink, w * 0.028f, Offset(dx, dotY)) else drawCircle(colors.ink, w * 0.028f, Offset(dx, dotY), style = Stroke(w * 0.008f))
    }
    // Keypad.
    val key = w * 0.16f
    repeat(9) { k ->
        val kx = cx + (k % 3 - 1) * key * 1.25f - key / 2f
        val ky = top + h * 0.6f + (k / 3) * key * 0.85f
        drawRoundRect(colors.cell, Offset(kx, ky), Size(key, key * 0.62f), CornerRadius(key * 0.18f))
    }
}
