# PortProof

PortProof is a native Android app for documenting technical installations on site: projects,
equipment, cable and port mapping, commissioning tests, defects, photo evidence and the PDF
handover pack. It is built for CCTV, network cabling, access control, AV and low-voltage
installers, and works fully offline with no account.

> Repository: `Rackfolio` (working repository name). Product name: **PortProof**.

## Status

**Phase 1 of 5 is complete** (foundation, design system, navigation, dashboard, projects).
See [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md) for what comes next.

What works today:

- **Home** (layout of the supplied reference design): bracket "P" logo, greeting with the
  technician's name and today's date over a soft glow, two glass tiles (*Active projects* with the
  active count, *Completed projects* with completed/total), a *Search a project* pill and a round
  *+* button for a new project. The avatar opens Settings and the grid button opens the Project
  overview. There is no tab bar; Home is the hub.
- **Project detail** (gradient header screen): status card with the schedule and a 5-step
  progress bar (tap to change status), and a *Project record* card. Rule-based checks highlight
  gaps in the record: yellow for a missing customer, site or dates (or an archived project), red
  for an overdue completion date, orange for a project due within 7 days or on hold. These are
  fixed rules, not AI. *Edit project* and a *…* menu (archive/restore, delete) sit at the bottom.
- **Project overview** (grid button): project counts that open the matching list, upcoming and
  overdue completion dates, and recent activity with *See all* for the full activity log.
- **Projects:** create, edit, change status, archive/restore (with undo), delete with confirmation.
  Fields: name, reference number, customer, site name and address, start/completion dates,
  description, status. List with filters (All, Active, Completed, Archived), sorting and search.
- **Search:** global project search, including archived projects.
- **Settings:** technician and company name, light/dark/system theme, privacy information.
- Room persistence with an activity log, validation of every input, unsaved-change protection,
  and form state that survives process death.
- Light and dark themes. On tablets and in landscape, content sits in a centred column (Home at
  most 600 dp wide, other screens at most 880 dp).

Equipment, cabling, QR codes, tests, photos, defects, PDF/CSV export and backup are **not built
yet**; they arrive in Phases 2–4 and will be reached from Home.

## Screenshots

Rendered by the Robolectric tests (`docs/screenshots/`), not captured on a device.

| Home | Home (dark) | Project detail | Project overview |
| --- | --- | --- | --- |
| ![Home](docs/screenshots/home_light.png) | ![Home dark](docs/screenshots/home_dark.png) | ![Project detail](docs/screenshots/project_detail.png) | ![Overview](docs/screenshots/overview_light.png) |

| Projects | Editor validation | Search | Settings |
| --- | --- | --- | --- |
| ![Projects](docs/screenshots/project_list.png) | ![Validation](docs/screenshots/02_editor_validation.png) | ![Search](docs/screenshots/08_search.png) | ![Settings](docs/screenshots/09_settings.png) |

Also rendered: [first run](docs/screenshots/home_first_run.png),
[detail (dark)](docs/screenshots/project_detail_dark.png),
[overview (dark)](docs/screenshots/overview_dark.png),
[activity log](docs/screenshots/activity_log.png),
[discard dialog](docs/screenshots/06_discard_dialog.png) and
[tablet](docs/screenshots/home_tablet.png).

## Tech stack

Kotlin 2.4, Jetpack Compose (Material 3), MVVM with the repository pattern, Room, Coroutines and
Flow, Hilt, DataStore, Navigation Compose with type-safe routes. Later phases add CameraX, ML Kit
barcode scanning, `PdfDocument`, WorkManager and the Storage Access Framework.
Exact versions and the reasoning behind them are in
[ARCHITECTURE.md](ARCHITECTURE.md#toolchain-and-compatibility).

| | |
| --- | --- |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 (Android 16, the current Google Play requirement) |
| Compile SDK | 37 |
| JDK | 17 or newer (builds and tests were verified with JDK 21) |

## Building

Requirements: JDK 17+, Android SDK with *Android SDK Platform 37* and *Build-Tools 36.0.0*, and an
Android Studio release that supports AGP 9.3.

```bash
./gradlew assembleDebug                       # debug APK (app/build/outputs/apk/debug)
./gradlew :core:model:test testDebugUnitTest  # all unit, Robolectric and UI-flow tests
./gradlew lintDebug                           # Android Lint, all modules
./gradlew assembleRelease                     # minified, unsigned release APK
```

The debug build installs as `com.portproof.app.debug`, so it can sit next to a release build.
Release signing is not configured in the repository by design (see Phase 5).

## Project structure

```
app/                  Application, MainActivity, app shell (snackbar host), nav graph
core/model            Pure Kotlin domain model and validation (no Android)
core/database         Room database, entities, DAOs, exported schemas
core/data             Repositories (offline-first), DataStore preferences, DI bindings
core/designsystem     Theme tokens, Inter font, icon set, reusable components
core/ui               Domain-aware shared UI (project card, status pill, activity row, greeting)
core/testing          Test fakes, dispatcher rule, screenshot helper
feature/dashboard     Home screen
feature/insights      Project overview, activity log
feature/projects      Project list, detail and editor
feature/search        Global search
feature/settings      Settings
build-logic/          Gradle convention plugins (shared module configuration)
tools/icons           Icon generator (Lucide SVG -> Compose ImageVector)
```

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md): modules, layers, navigation, design system, security,
  toolchain and architecture decisions
- [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md): current schema (v1) and the planned schema for all phases
- [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md): phases, open issues, technical risks
- [TESTING.md](TESTING.md): test strategy, how to run tests, current results
- [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md): bundled font and icon licences

## Privacy

PortProof has no backend, no account and no internet permission. Data is stored in app-private
storage on the device. Android's own device backup may include the project database (end-to-end
encrypted backups only); see [ARCHITECTURE.md](ARCHITECTURE.md#security-and-privacy).
