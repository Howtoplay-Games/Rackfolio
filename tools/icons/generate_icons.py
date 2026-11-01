#!/usr/bin/env python3
"""Generates PortProofIcons.kt (Compose ImageVectors) from Lucide icon nodes.

Usage:
  npm pack lucide-static@<version> && tar -xzf lucide-static-<version>.tgz
  python3 tools/icons/generate_icons.py package/icon-nodes.json

Icons are listed in icons.txt as "<lucide-name> <KotlinName>". Lucide is ISC
licensed; see THIRD_PARTY_NOTICES.md. Stroke width is reduced from Lucide's
default 2.0 to match the PortProof design language.
"""
import json
import math
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
OUT = ROOT / "core/designsystem/src/main/kotlin/com/portproof/core/designsystem/icon/PortProofIcons.kt"
STROKE = 1.75


def fmt(v):
    return ("%.3f" % float(v)).rstrip("0").rstrip(".")


def element_to_path(tag, a):
    if tag == "path":
        return a["d"]
    if tag == "line":
        return f"M{fmt(a['x1'])} {fmt(a['y1'])}L{fmt(a['x2'])} {fmt(a['y2'])}"
    if tag in ("polyline", "polygon"):
        pts = a["points"].replace(",", " ").split()
        pairs = [f"{fmt(pts[i])} {fmt(pts[i + 1])}" for i in range(0, len(pts), 2)]
        return "M" + "L".join(pairs) + ("Z" if tag == "polygon" else "")
    if tag in ("circle", "ellipse"):
        cx, cy = float(a["cx"]), float(a["cy"])
        rx = float(a.get("r", a.get("rx", 0)))
        ry = float(a.get("r", a.get("ry", 0)))
        return (f"M{fmt(cx - rx)} {fmt(cy)}a{fmt(rx)} {fmt(ry)} 0 1 0 {fmt(2 * rx)} 0"
                f"a{fmt(rx)} {fmt(ry)} 0 1 0 {fmt(-2 * rx)} 0")
    if tag == "rect":
        x, y = float(a.get("x", 0)), float(a.get("y", 0))
        w, h = float(a["width"]), float(a["height"])
        rx = float(a.get("rx", a.get("ry", 0)))
        rx = min(rx, w / 2, h / 2)
        if rx <= 0:
            return f"M{fmt(x)} {fmt(y)}h{fmt(w)}v{fmt(h)}h{fmt(-w)}Z"
        return (f"M{fmt(x + rx)} {fmt(y)}h{fmt(w - 2 * rx)}a{fmt(rx)} {fmt(rx)} 0 0 1 {fmt(rx)} {fmt(rx)}"
                f"v{fmt(h - 2 * rx)}a{fmt(rx)} {fmt(rx)} 0 0 1 {fmt(-rx)} {fmt(rx)}"
                f"h{fmt(-(w - 2 * rx))}a{fmt(rx)} {fmt(rx)} 0 0 1 {fmt(-rx)} {fmt(-rx)}"
                f"v{fmt(-(h - 2 * rx))}a{fmt(rx)} {fmt(rx)} 0 0 1 {fmt(rx)} {fmt(-rx)}Z")
    raise ValueError(f"Unsupported SVG element: {tag}")


HEADER = '''// GENERATED FILE - do not edit by hand.
// Source: Lucide icons (ISC License, https://lucide.dev). Regenerate with
// tools/icons/generate_icons.py after editing tools/icons/icons.txt.
package com.portproof.core.designsystem.icon

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Outline icon set used across PortProof. Icons are tinted by the `Icon` composable. */
object PortProofIcons {
'''

FOOTER = '''}

private fun lucideIcon(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        paths.forEach { pathData ->
            addPath(
                pathData = addPathNodes(pathData),
                fill = null,
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeLineWidth = STROKE_WIDTH,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

private const val STROKE_WIDTH = %sf
'''


def main():
    nodes = json.load(open(sys.argv[1], encoding="utf-8"))
    entries = []
    for line in (ROOT / "tools/icons/icons.txt").read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        lucide_name, kotlin_name = line.split()
        if lucide_name not in nodes:
            sys.exit(f"Unknown Lucide icon: {lucide_name}")
        paths = [element_to_path(tag, attrs) for tag, attrs in nodes[lucide_name]]
        args = ",\n            ".join(json.dumps(p) for p in paths)
        entries.append(
            f"    val {kotlin_name}: ImageVector by lazy {{\n"
            f"        lucideIcon(\n            \"{kotlin_name}\",\n            {args},\n        )\n    }}\n"
        )
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(HEADER + "\n".join(entries) + FOOTER % fmt(STROKE), encoding="utf-8")
    print(f"Wrote {len(entries)} icons to {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
