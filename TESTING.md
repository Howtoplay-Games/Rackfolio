# Testing

## Strategy

| Layer | Tool | Where | What it proves |
| --- | --- | --- | --- |
| Domain | JUnit (JVM) | `core:model` | Validation, sanitising, derived values |
| Database | Robolectric + in-memory Room | `core:database` | SQL correctness: filters, sorting, escaping, aggregates, cascades, foreign keys |
| Repositories | Robolectric + in-memory Room, temp-file DataStore | `core:data` | Transactions, activity logging, timestamps, not-found handling, time-zone bucketing |
| ViewModels | JUnit + coroutines-test + fakes from `core:testing` | `feature:*` | UI state, validation flow, error handling, process-death restore |
| App flows | Robolectric + Compose UI test against the real app (Hilt, Room, DataStore) | `app` | End-to-end user journeys, navigation and back handling |
| Rendering | Robolectric native graphics | `app`, `feature:dashboard`, `feature:insights`, `feature:projects` | PNG renders for design review (`build/screenshots`, copied to `docs/screenshots`) |

Fakes (`TestProjectRepository`, `TestActivityRepository`, `TestUserPreferencesRepository`) live in
`core:testing`; `TestProjectRepository.failWrites` simulates storage failures. Time is controlled
by injecting `java.time.Clock`.

## Running

```bash
./gradlew :core:model:test testDebugUnitTest   # everything (about 1–2 minutes warm)
./gradlew :core:database:testDebugUnitTest      # one module
./gradlew :app:testDebugUnitTest --tests '*CreateProjectFlowTest*'
./gradlew lintDebug
```

The first Robolectric run downloads the Android 16 (SDK 36) runtime jar from Maven Central. On
JDK 17+ the required `--add-opens` flags are applied by the build logic. Rendered screenshots are
written to `<module>/build/screenshots/`. They are side effects, not declared task outputs, so a
test task restored from the Gradle build cache writes none: regenerate them with
`./gradlew testDebugUnitTest --rerun` (or `--rerun-tasks`).

## Results (1 November 2026, after the third interface rebuild)

All 75 tests pass; `lintDebug` reports no issues in any module; `assembleDebug` and the minified
`assembleRelease` both succeed.

| Suite | Tests |
| --- | --- |
| `core:model` — `ProjectValidatorTest`, `ActivityTrendTest` | 11 |
| `core:database` — `ProjectDaoTest` (incl. deadlines) | 9 |
| `core:data` — `OfflineFirstProjectRepositoryTest`, `DataStoreUserPreferencesRepositoryTest` | 11 |
| `feature:projects` — editor, detail, list ViewModels, `ScheduleStateTest`, `ProjectReviewTest` (highlight rules), `ProjectScreensScreenshotTest` | 26 |
| `feature:dashboard` — `DashboardViewModelTest`, `DashboardScreenshotTest` | 6 |
| `feature:insights` — `InsightsViewModelTest`, `InsightsScreenshotTest` | 5 |
| `feature:search`, `feature:settings` — ViewModel tests | 4 |
| `app` — `CreateProjectFlowTest`: create a project from Home (validation, detail, *X* back to Home, Active tile count and list); unsaved-changes guard; every Home entry point opens its screen (overview and activity log, search, settings, both tiles) | 3 |

## Not covered yet

- **No emulator or physical device run.** The build environment has no emulator, so behaviour such
  as the system splash screen, predictive back animation, IME handling and real rendering
  performance has not been observed on hardware. Robolectric renders are close to device output
  but not identical.
- No instrumented (`androidTest`) tests, screenshot diffing, accessibility scanner checks or
  performance benchmarks yet (Phase 5).
- No migration tests yet: there is only schema version 1. Every later version adds them.

## Manual check-list for Phase 1 (run on a device)

1. First launch shows the splash screen, then Home; no light/dark flash.
2. Create a project with only a name; create one with every field and both dates.
3. Try to save without a name, with a 121-character name, and with the completion date before the
   start date: each shows an inline error and nothing is saved.
4. Type in the editor, rotate the device, then put the app in the background and kill the process
   (`adb shell am kill com.portproof.app.debug`): the typed values come back.
5. Press Back with unsaved changes: the discard dialog appears; "Keep editing" keeps the text.
6. Change status on the detail screen; archive (then Undo); restore from the Archived filter;
   delete with confirmation. Home tiles, overview counts, activity and lists update immediately.
7. Search for part of a customer name, a reference number and `50%` (literal match).
8. Switch theme in Settings (system, light, dark) and check every screen in both themes.
9. Open a project from a scrolled list, go back: scroll position and filter are kept. The *X* on
   project detail returns straight to Home.
10. Tablet or unfolded foldable: content is centred and readable; headers and glow fill the width.
11. TalkBack: every icon button is announced with a label; tiles read their counts; the status
    progress reads its step; each highlight is read as a sentence.
12. Font size at maximum: no clipped text on Home tiles, the review card, list cards or the editor.
13. Home: the avatar opens Settings, the grid button the Project overview, each tile its filtered
    list, the search pill Search and *+* the editor.
14. Project detail: an overdue project shows a red highlight, a project due within 7 days or on
    hold an orange one, a missing customer, site or dates a yellow one; tapping the status card
    changes status; the *…* menu archives, restores and deletes.
