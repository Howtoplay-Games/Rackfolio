# Third-party notices

PortProof bundles the following third-party assets. Their licence texts are in [`licenses/`](licenses/).

| Asset | Where it is used | Licence |
| --- | --- | --- |
| [Inter](https://rsms.me/inter/) 4.1 by Rasmus Andersson (Regular, Medium, SemiBold, Bold) | `core/designsystem/src/main/res/font/` | SIL Open Font License 1.1 — [`licenses/Inter-OFL-1.1.txt`](licenses/Inter-OFL-1.1.txt) |
| [Fluent Emoji](https://github.com/microsoft/fluentui-emoji) by Microsoft, 3D style (19 images, resized and converted to WebP by `tools/emoji/fetch_emoji.py`) | `core/designsystem/src/main/res/drawable-nodpi/` | MIT — [`licenses/FluentEmoji-MIT.txt`](licenses/FluentEmoji-MIT.txt) |
| [Lucide](https://lucide.dev) icons 1.66.0 (converted to Compose `ImageVector`s, stroke width changed to 1.75) | `core/designsystem/.../icon/PortProofIcons.kt` | ISC; some icons derive from Feather (MIT) — [`licenses/Lucide-ISC.txt`](licenses/Lucide-ISC.txt) |

Libraries resolved through Gradle (AndroidX, Kotlin and kotlinx, Dagger/Hilt) are Apache License 2.0.

An in-app "Open-source licences" screen listing all of the above is planned for Phase 5, before
the first Google Play release; the font licence must ship with the app binary.
