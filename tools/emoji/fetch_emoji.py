#!/usr/bin/env python3
"""Downloads Microsoft Fluent 3D emoji (MIT licence) and converts them to WebP drawables.

Usage (needs Pillow):  python3 tools/emoji/fetch_emoji.py

Badges are small emoji used in metric cards (96 px); illustrations are the larger artwork on
feature cards (256 px, the source resolution). Bundling them keeps the artwork identical on every
device instead of depending on the vendor emoji font. Edit ASSETS to add or replace artwork.
"""
import io
import pathlib
import urllib.parse
import urllib.request

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parents[2]
OUT = ROOT / "core/designsystem/src/main/res/drawable-nodpi"
BASE = "https://raw.githubusercontent.com/microsoft/fluentui-emoji/main/assets/"

# (Fluent asset folder, resource name, pixel size)
ASSETS = [
    ("Hammer and wrench", "emoji_tools", 96),
    ("Spiral calendar", "emoji_calendar", 96),
    ("Check mark button", "emoji_check", 96),
    ("Pause button", "emoji_pause", 96),
    ("File cabinet", "emoji_archive", 96),
    ("File folder", "emoji_folder", 96),
    ("Chart increasing", "emoji_chart", 96),
    ("Hourglass done", "emoji_hourglass", 96),
    ("Bar chart", "emoji_bar_chart", 96),
    ("Memo", "emoji_memo", 96),
    ("Video camera", "illus_camera", 256),
    ("Desktop computer", "illus_computer", 256),
    ("Key", "illus_key", 256),
    ("Speaker high volume", "illus_speaker", 256),
    ("Television", "illus_tv", 256),
    ("Electric plug", "illus_plug", 256),
    ("Satellite antenna", "illus_antenna", 256),
    ("Toolbox", "illus_toolbox", 256),
    ("Building construction", "illus_construction", 256),
]


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for folder, name, size in ASSETS:
        file_name = folder.lower().replace(" ", "_") + "_3d.png"
        url = BASE + urllib.parse.quote(f"{folder}/3D/{file_name}")
        with urllib.request.urlopen(url, timeout=30) as response:
            image = Image.open(io.BytesIO(response.read())).convert("RGBA")
        if image.width != size:
            image = image.resize((size, size), Image.LANCZOS)
        target = OUT / f"{name}.webp"
        image.save(target, "WEBP", quality=90, method=6)
        print(f"{target.relative_to(ROOT)}  {target.stat().st_size // 1024} KB")


if __name__ == "__main__":
    main()
