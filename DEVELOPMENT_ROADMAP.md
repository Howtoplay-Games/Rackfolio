# Development roadmap

Each phase ends with: compile (debug and minified release), all automated tests green, Lint
clean, an architecture review, updated documentation, and a list of remaining issues.

| Phase | Scope | Status |
| --- | --- | --- |
| 1 | Foundation: project setup, architecture, Gradle, design system, navigation, dashboard, projects, Room | **Done** (1 Nov 2026) |
| 1b | Interface rebuilt to the second reference design (Home, Insight, Metrics Summary, activity log, five-tab bar) | **Done** (1 Nov 2026), superseded by 1c |
| 1c | Interface rebuilt to the third reference design (glass Home hub, gradient project detail with highlighted record, Project overview, no tab bar) | **Done** (1 Nov 2026), superseded by 1d |
| 1d | Login (PIN app lock with fingerprint unlock), PortProof Pro subscription page with a free-plan limit, interface rebuilt to the fourth reference design (dashboard gauge, project cards, Projects tab with category tiles, dark five-tab bar) | **Done** (1 Nov 2026) |
| 2 | Equipment management, cable and port mapping, QR generation and scanning | Next |
| 3 | Commissioning and test records, photo evidence, defect tracking | Planned |
| 4 | PDF handover reports, CSV export, backup and restore | Planned |
| 5 | Hardening: testing, performance, accessibility, security review, Play release | Planned |

## Phase 1 — delivered

- Multi-module Gradle project with convention plugins, version catalog and a checksum-verified
  Gradle wrapper (AGP 9.3.3, Kotlin 2.4.21, compile SDK 37, target SDK 36, min SDK 26).
- Domain model and validation (`core:model`), Room v1 schema with exported JSON, offline-first
  repositories with transactional activity logging, DataStore preferences, Hilt wiring.
- Design system derived from the reference design: tokens for light and navy/graphite dark themes,
  Inter typography, generated Lucide icon set, and the components listed in ARCHITECTURE.md.
- Navigation: type-safe routes, three top-level destinations with saved back stacks, floating bar
  on phones and rail on wider screens, back handling, unsaved-change guard.
- Screens: dashboard, project list (filters, sort, search), project detail (status, schedule
  progress, activity, archive/restore with undo, delete with confirmation), project editor
  (create/edit with validation and process-death restore), global search, settings.
- Launcher icon (adaptive, with themed monochrome layer), splash screen, backup rules.
- 56 automated tests, all passing; Lint clean; see TESTING.md.

## Phase 1b — interface rebuilt to the reference design

- New visual language: white canvas, black hero cards and pill buttons, violet accent, pastel
  feature cards, bundled 3D emoji artwork, dark floating five-item navigation pill.
- Home rebuilt (greeting, Project Progress ring, Overview metrics, Active Projects feature cards).
- New `feature:insights` module: Metrics Summary tab, Insight screen, activity log.
- Data additions: upcoming-deadline query and hour-of-day activity insights.
- 70 automated tests, all passing; Lint clean.

## Phase 1c — interface rebuilt to the third reference design

- New visual language: cool canvas with peach/lavender glow, glass tiles, bracket "P" logo (also
  the launcher icon), periwinkle gradient headers, white document cards, black pill buttons.
- Home is the hub (no tab bar): Active/Completed tiles, search pill, *+*, grid button to the
  Project overview, avatar to Settings.
- Project detail: status card with schedule and 5-step progress, project record with rule-based
  highlights (missing data, overdue, due soon, on hold, archived), *Edit project* and a *…* menu.
- Insight and Metrics Summary merged into one Project overview screen; charts, the floating bar,
  the navigation rail and the bundled emoji artwork were removed.
- 75 automated tests, all passing; Lint clean.

## Phase 1d — login, PortProof Pro, fourth reference design

- New visual language from the fourth reference (a calorie tracker): warm off-white canvas, white
  cards with a peach glow, orange accent used as a fill, near-black buttons and a dark bottom bar
  with an orange centre button. The glass components, the in-app logo and the greeting were
  removed (the launcher icon is unchanged).
- Home: *Dashboard* title between calendar and bell buttons, an 11-segment gauge of completed
  projects, a wide *+* bar and the three most recently updated projects.
- Project cards in the reference's meal-card layout (schedule figure, start, due, status, edit).
  Projects tab with search, sort, category tiles and a count badge.
- Five tabs again (ADR-008): Home, Projects, Search (centre), Overview, Settings.
- New `feature:login`: welcome pages with drawn illustrations, profile, 6-digit PIN with
  confirmation, optional fingerprint unlock, unlock screen with wrong-PIN waits, Login & security
  (change PIN, fingerprint, lock now), *Forgot PIN?* → erase all data (ADR-012).
- New `core:billing` and `feature:subscription`: PortProof Pro through Google Play Billing 9.1.0;
  the free plan allows 3 active projects, enforced in the repository (ADR-013). Prices and trials
  come from Google Play; a debug-only switch simulates Pro.
- Libraries now merge in `INTERNET`, `ACCESS_NETWORK_STATE`, `BILLING`, `USE_BIOMETRIC` and
  `USE_FINGERPRINT` (ADR-011); the PIN and the cached plan are excluded from backups.
- 114 automated tests, all passing; Lint clean.

### Remaining issues after Phase 1d

| Issue | Plan |
| --- | --- |
| Not yet run on a physical device or emulator; verification used Robolectric renders and flow tests | Smoke-test on devices (API 26, 30, 34, 36, a foldable and a tablet) at the start of Phase 2, including the biometric prompt and the manual check-list in TESTING.md |
| PortProof Pro has not been bought for real: the product does not exist in Play Console yet | Create `portproof_pro` with a monthly and a yearly base plan, upload to the internal testing track, add licence testers and run check-list items 22–27 |
| Purchases are verified by Google Play on the device only (no server check) | Accepted for now (ADR-013); revisit if Pro ever unlocks more than the project limit |
| The Data safety form and privacy policy must cover Google Play Billing's own data handling (ADR-011) | Phase 5, before release |
| A corrupted database file is deleted by the framework's default handler | Phase 4: keep a copy of the corrupted file and offer restore from backup |
| The Recents thumbnail is hidden only on Android 13+ (`FLAG_SECURE` would also block screenshots) | Decide with users in the Phase 2 device tests |
| Welcome-art labels are placed for English text lengths | Check the pins when the first translation is added |
| Activity log grows without limit | Phase 5: retention policy (for example, compact entries older than two years) |
| Search folds ASCII case only (`COLLATE NOCASE`) | Phase 5: evaluate FTS4 with a Unicode tokenizer |
| No in-app open-source licence screen yet | Phase 5, before release (required for the bundled font) |
| `applicationId` `com.portproof.app` is provisional | Confirm before the first Play upload; it cannot change afterwards |
| Overview cards for planned and on-hold projects are not tappable (the project list has no filter for them) | Phase 2: add status filters to the project list |

## Phase 2 — equipment, cabling, QR

- Schema v2 (`equipment`, `cable_connections`) with auto-migration and migration tests.
- Equipment register per project and a global **Equipment** screen opened from Home (search across projects by name,
  model, serial number, location, IP/MAC). Validators for IP and MAC addresses.
- Cable and port mapping: connection editor with equipment pickers limited to the same project,
  port-reuse warning, VLAN range checks. Mapping views: per-equipment port grid (uses
  `port_count`), and a connection list grouped by source equipment showing A → B with cable ID,
  type, VLAN and status.
- QR codes: payload `portproof:v1:<type>:<uuid>` (no personal data). Generation with ZXing,
  display full-screen at maximum brightness, label export as PNG/PDF sheet. Scanner with CameraX
  and ML Kit (bundled model, offline); camera permission requested on first use with a rationale;
  unknown, foreign or deleted-record codes handled with clear messages; deep links into records.
- Project detail gains section cards (equipment, cabling) as contextual navigation.
- Exit criteria: device smoke test of the scanner on at least two phones; migration test 1→2.

## Phase 3 — commissioning, photos, defects

- Schema v3 (`test_records`, `defects`, `photos`).
- Test records per equipment item or connection (pass / fail / not tested, measured vs expected).
  Test lists show pass/fail counts; the dashboard chart gains pass/fail series.
- Photos: capture with CameraX into app-private storage (with EXIF orientation applied and size
  capped), import with the system photo picker (no storage permission), captions, viewer, delete.
  Orphan-file cleanup with WorkManager.
- Defects linked to a project, equipment, connection or test, with severity, status, resolution
  notes and resolved date; open-defect counts on the dashboard and project detail.

## Phase 4 — reports, export, backup

- PDF handover report with `PdfDocument`: cover (project, customer, site, technician, company,
  generation date), equipment register, cable/port map, test results, defects and resolution
  status, selected photos; repeated table headers, page numbers and footers; generated off the main
  thread with progress and cancellation; in-app preview (`PdfRenderer`) and sharing through
  `FileProvider` and the Android share sheet. A **Reports** screen, opened from Home, lists generated reports.
- CSV export (RFC 4180 quoting, UTF-8 with BOM for spreadsheet compatibility, formula-injection
  protection for cells starting with `=`, `+`, `-`, `@`) for equipment, connections, tests and
  defects, saved through the Storage Access Framework.
- Backup and restore: versioned ZIP (JSON data + photos) written and read through the Storage
  Access Framework, validated before import, imported in one transaction; corrupted-database
  recovery path.

## Phase 5 — hardening and release

- Testing: instrumented tests on emulators/devices, screenshot regression tests, migration tests
  for every version, large-dataset tests (thousands of equipment and connection records).
- Performance: baseline profile, startup measurement, list and PDF generation benchmarks,
  Compose stability review using compiler reports (`-Pportproof.composeReports=true`).
- Accessibility: TalkBack and Switch Access passes, font scaling to 200%, contrast re-check.
- Security review: dependency audit, backup rules verification (AGP backup tests), `FileProvider`
  paths, export file permissions, R8 rules review, logging audit.
- Release: signing (Play App Signing; keys outside the repository), Play Console listing and the
  PortProof Pro subscription, Data safety form (PortProof's own code collects nothing; declare what
  Google Play Billing requires, ADR-011), privacy policy, in-app licences screen, localisation
  plan, versioning and release notes process.

## Technical risks

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Toolchain churn (AGP 9.x, Kotlin 2.4, KSP2, Hilt) breaks builds | High | Versions pinned in one catalog; upgrade as a set, only within published compatibility ranges; CI build on every change |
| Schema evolution across four versions loses data | High | Exported schemas, auto-migrations, `MigrationTestHelper` tests, no destructive fallback, backup before risky upgrades |
| Cross-project or dangling references in cabling data | High | Composite foreign keys onto `equipment(project_id, id)`, transactional checks |
| Camera and ML Kit behaviour differs across devices (focus, low light, permission denial) | Medium | Torch toggle, manual entry fallback, graceful permission-denied state, device test matrix |
| Bundled ML Kit model adds ~3–6 MB per ABI | Medium | App Bundle per-ABI splits; measure in Phase 2 |
| Photo storage grows large; cloud backup quota (25 MB) | Medium | Downscale and compress on capture; photos excluded from cloud backup; in-app ZIP backup |
| `PdfDocument` has no layout engine: pagination, long tables, image memory | Medium | Own measured layout with tested pagination, sampled bitmap decoding, generation off the main thread |
| Restore of a backup made by a newer app version | Medium | Version field in the backup manifest; refuse newer versions with a clear message |
| Corrupted database file | Medium | Recovery handler (Phase 4) and user-controlled backups |
| Large datasets slow down `LIKE` search and dashboards | Low–medium | Indices in place; FTS if measurements require it |
| Google Play Billing behaves differently from the fakes (pending payments, account holds, refunds, Play Store updates) | Medium | Licence-tester runs on the internal track with Play's approving, declining and slow test cards before release; the cached plan never deletes or hides data |
| A forgotten PIN means erasing all data | Medium | Clear warnings in setup and *Forgot PIN?*; the Phase 4 backup gives users a copy they control |

## Decisions needed from the product owner

1. Confirm the `applicationId` (currently `com.portproof.app`) and the publisher/package domain.
2. Confirm the product name "PortProof" (the repository is named Rackfolio).
3. Confirm min SDK 26 (Android 8.0) or raise it to reduce the test matrix.
4. Launch languages and markets (affects date formats, units and translations).
5. PortProof Pro: prices for the monthly and yearly plans, whether to offer a free trial, and the
   free plan's limit (3 active projects, `PlanRules.FREE_ACTIVE_PROJECT_LIMIT`).
