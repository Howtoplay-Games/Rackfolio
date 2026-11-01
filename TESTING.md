# Testing

## Strategy

| Layer | Tool | Where | What it proves |
| --- | --- | --- | --- |
| Domain | JUnit (JVM) | `core:model` | Validation, sanitising, derived values, free-plan limit, PIN rules and wrong-PIN waits |
| Database | Robolectric + in-memory Room | `core:database` | SQL correctness: filters, sorting, escaping, aggregates, cascades, foreign keys |
| Repositories | Robolectric + in-memory Room, temp-file DataStore | `core:data` | Transactions, activity logging, timestamps, not-found handling, time-zone bucketing, the plan limit inside write transactions, PIN hashing and stored lockout |
| Billing | JUnit (JVM) | `core:billing` | Choosing plans and offers from Google Play's catalogue: eligible trials, skipped offers, same-currency savings |
| ViewModels | JUnit + coroutines-test + fakes from `core:testing` (Robolectric for the paywall) | `feature:*` | UI state, validation flow, error handling, process-death restore, login setup, unlock and lock timeout, purchase outcomes |
| App flows | Robolectric + Compose UI test against the real app (Hilt, Room, DataStore, the real billing client) | `app` | End-to-end journeys: login setup, projects, tabs and back handling, lock and unlock, the free-plan limit |
| Rendering | Robolectric native graphics | `app` and the dashboard, insights, projects, login and subscription features | PNG renders for design review (`build/screenshots`, copied to `docs/screenshots`) |

Fakes live in `core:testing`: `TestProjectRepository` (`failWrites` simulates storage failures,
`activeLimit` applies the free plan), `TestActivityRepository`, `TestUserPreferencesRepository`,
`TestPlanRepository`, `TestAppLockRepository`, `TestSubscriptionRepository` (a sample catalogue:
€4.99 a month with a 7-day trial and €39.99 a year) and `MutableTestClock`. Time is controlled by
injecting `java.time.Clock`.

Under Robolectric the real Google Play Billing client reports billing as unavailable, so the app
flows run on the free plan. Robolectric also reports an enrolled fingerprint, so the setup flow
offers fingerprint unlock and the tests answer *Not now*.

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

Two capture limitations: Robolectric never finishes press-ripple animations, so the app flow opens
tabs through their accessibility click action (same click handler, no ripple) before taking a
render; and dialog windows are drawn at the top of the capture without the dimmed background.

## Results (1 November 2026, Phase 1d)

All 114 tests pass; `lintDebug` reports no issues in any module; `assembleDebug` and the minified
`assembleRelease` both succeed.

| Suite | Tests |
| --- | --- |
| `core:model` — `ProjectValidatorTest`, `ActivityTrendTest`, `PlanAndLockRulesTest` | 14 |
| `core:database` — `ProjectDaoTest` (incl. deadlines) | 9 |
| `core:data` — `OfflineFirstProjectRepositoryTest` (incl. free-plan limit on create, reopen and restore), `DataStoreAppLockRepositoryTest`, `DataStoreUserPreferencesRepositoryTest` | 17 |
| `core:billing` — `PlanSelectionTest` | 3 |
| `core:designsystem` — `GaugeSegmentsTest` | 1 |
| `feature:projects` — editor (incl. limit notice and refused save), detail and list ViewModels, `ScheduleStateTest`, `ProjectReviewTest` (highlight rules), `ProjectScreensScreenshotTest` | 28 |
| `feature:dashboard` — `DashboardViewModelTest`, `DashboardScreenshotTest` | 6 |
| `feature:insights` — `InsightsViewModelTest`, `InsightsScreenshotTest` | 5 |
| `feature:login` — `SetupViewModelTest`, `UnlockAndSecurityViewModelTest`, `LoginScreenshotTest` | 16 |
| `feature:subscription` — `PaywallViewModelTest`, `PaywallScreenshotTest` | 7 |
| `feature:search`, `feature:settings` — ViewModel tests | 4 |
| `app` — `CreateProjectFlowTest`: first-run login setup then create a project from Home (validation, detail, *X* back to Home, list); unsaved-changes guard; every tab and Home header button opens its screen, *Lock now* asks for the PIN (wrong, then right), Back from a tab returns Home; the free plan stops at three active projects and points to Pro | 4 |

## Not covered yet

- **No emulator or physical device run.** The build environment has no emulator, so behaviour such
  as the system splash screen, the biometric prompt, predictive back, IME handling and real
  rendering performance has not been observed on hardware. Robolectric renders are close to device
  output but not identical.
- **No real purchase.** Buying, trials, pending payments, cancellation and renewal need the
  Play Console setup described in the README and licence testers. `PlayBillingSubscriptionRepository`
  (the Google Play connection) has no automated tests; plan selection, the paywall and the limit
  are tested against fakes.
- *Erase all data* (`clearApplicationUserData()`) ends the process, so it is not run in tests.
- No instrumented (`androidTest`) tests, screenshot diffing, accessibility scanner checks or
  performance benchmarks yet (Phase 5).
- No migration tests yet: there is only schema version 1. Every later version adds them.

## Manual check-list (run on a device)

Projects and navigation:

1. First launch shows the splash screen, then the welcome pages; no light/dark flash.
2. Create a project with only a name; create one with every field and both dates.
3. Try to save without a name, with a 121-character name, and with the completion date before the
   start date: each shows an inline error and nothing is saved.
4. Type in the editor, rotate the device, then put the app in the background and kill the process
   (`adb shell am kill com.portproof.app.debug`): after unlocking, the typed values come back.
5. Press Back with unsaved changes: the discard dialog appears; "Keep editing" keeps the text.
6. Change status on the detail screen; archive (then Undo); restore from the Archived tile;
   delete with confirmation. The Home gauge, Overview counts, activity and lists update at once.
7. Search for part of a customer name, a reference number and `50%` (literal match).
8. Switch theme in Settings (system, light, dark) and check every screen in both themes.
9. Switch between all five tabs: each keeps its scroll position and filter; Back from a tab
   returns Home, Back from Home leaves the app. The *X* on project detail returns straight Home.
10. Home: the calendar button opens active projects sorted by start date, the bell the activity
    log, the *+* bar the editor, a card its project and a card's pencil the editor.
11. Tablet or unfolded foldable: content is centred and readable; the bar and headers fill the
    width.
12. TalkBack: every icon button and bar item is announced with a label (bar items as tabs); the
    gauge reads "N of M projects completed"; the PIN dots read the digits entered; each highlight
    is read as a sentence; nothing behind the login screen is reachable while locked.
13. Font size at maximum: no clipped text on Home, cards, the PIN pad, the Pro page or the editor.

Login:

14. Setup refuses 111111 and 123456 and a mismatched confirmation; after setup the app opens.
15. Kill and restart the app: it asks for the PIN. Leave it in the background for under 5 minutes:
    no PIN; for 5 minutes or more: PIN.
16. Enter a wrong PIN five times: a 30-second wait starts, the keypad is disabled, and the wait
    survives a restart; the next wrong PIN waits 1 minute.
17. With a fingerprint enrolled: turn on fingerprint unlock in setup or in Login & security
    (needs one scan), unlock with it, cancel the prompt and use the PIN instead.
18. Change the PIN (wrong current PIN is refused); *Lock now* locks at once.
19. *Forgot PIN?* → *Erase all data* deletes everything and restarts at the welcome pages.
20. Restore a device backup or transfer to a new phone: projects come back, the app asks for a new
    PIN, and the plan is checked with Google Play again.
21. Android 13+: the app's Recents thumbnail is blank.

PortProof Pro (internal testing track, licence tester account):

22. The Pro page shows the Play Console prices in the account's currency, the free trial when
    eligible, and "Save N%" only for same-currency plans.
23. Buy the monthly plan with a test card: "Welcome to PortProof Pro", the limit disappears,
    Settings shows PortProof Pro. Restart offline: still Pro.
24. Use the "slow test card" for a pending purchase: the page says the payment is pending; Pro
    starts once it completes. A declined card leaves the free plan.
25. Cancel in Google Play and let the test subscription expire: after the next check the free plan
    applies again; existing projects stay, new active projects beyond 3 are refused with *See Pro*.
26. *Restore purchases* on a second device with the same account turns Pro on.
27. A release build has no *Treat this device as Pro* switch.
