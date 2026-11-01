# Third-party notices

PortProof bundles the following third-party assets. Their licence texts are in [`licenses/`](licenses/).

| Asset | Where it is used | Licence |
| --- | --- | --- |
| [Inter](https://rsms.me/inter/) 4.1 by Rasmus Andersson (Regular, Medium, SemiBold, Bold) | `core/designsystem/src/main/res/font/`; the Bold "P" outline is also used as a vector path in the launcher icon (`ic_launcher_foreground.xml`, `ic_launcher_monochrome.xml`) | SIL Open Font License 1.1 — [`licenses/Inter-OFL-1.1.txt`](licenses/Inter-OFL-1.1.txt) |
| [Lucide](https://lucide.dev) icons 1.66.0 (converted to Compose `ImageVector`s, stroke width changed to 1.75) | `core/designsystem/.../icon/PortProofIcons.kt` | ISC; some icons derive from Feather (MIT) — [`licenses/Lucide-ISC.txt`](licenses/Lucide-ISC.txt) |

Libraries resolved through Gradle:

- AndroidX (including androidx.biometric), Kotlin and kotlinx, Dagger/Hilt and Google's
  DataTransport runtime (`com.google.android.datatransport`) are Apache License 2.0.
- The Google Play Billing Library (`com.android.billingclient:billing`, `billing-ktx`) and the Google
  Play services libraries it brings in (`play-services-base`, `-basement`, `-tasks`, `-location`,
  `-places-placereport`) are distributed under the
  [Android Software Development Kit License](https://developer.android.com/studio/terms).

An in-app "Open-source licences" screen listing all of the above is planned for Phase 5, before
the first Google Play release; the font licence must ship with the app binary.
