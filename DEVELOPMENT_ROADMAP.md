# Development roadmap

Each phase ends with: compile (debug and minified release), all automated tests green, Lint
clean, an architecture review, updated documentation, and a list of remaining issues.

| Phase | Scope | Status |
| --- | --- | --- |
| 1 | Foundation: project setup, architecture, Gradle, design system, navigation, dashboard, projects, Room | **Done** (1 Nov 2026) |
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

### Remaining issues after Phase 1

| Issue | Plan |
| --- | --- |
| Not yet run on a physical device or emulator; verification used Robolectric renders and flow tests | Smoke-test on devices (API 26, 30, 34, 36, a foldable and a tablet) at the start of Phase 2 |
| A corrupted database file is deleted by the framework's default handler | Phase 4: keep a copy of the corrupted file and offer restore from backup |
| The deprecated v1 Compose test rule is used for the activity-level flow test (the v2 rule does not go idle while the splash screen is kept on screen) | Phase 5: investigate a test-only splash condition |
| Activity log grows without limit | Phase 5: retention policy (for example, compact entries older than two years) |
| Search folds ASCII case only (`COLLATE NOCASE`) | Phase 5: evaluate FTS4 with a Unicode tokenizer |
| No in-app open-source licence screen yet | Phase 5, before release (required for the bundled font) |
| `applicationId` `com.portproof.app` is provisional | Confirm before the first Play upload; it cannot change afterwards |

## Phase 2 — equipment, cabling, QR

- Schema v2 (`equipment`, `cable_connections`) with auto-migration and migration tests.
- Equipment register per project and a global **Equipment** tab (search across projects by name,
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
  `FileProvider` and the Android share sheet. **Reports** tab lists generated reports.
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
- Release: signing (Play App Signing; keys outside the repository), Play Console listing, data
  safety form (no data collected or shared), privacy policy, in-app licences screen, localisation
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

## Decisions needed from the product owner

1. Confirm the `applicationId` (currently `com.portproof.app`) and the publisher/package domain.
2. Confirm the product name "PortProof" (the repository is named Rackfolio).
3. Confirm min SDK 26 (Android 8.0) or raise it to reduce the test matrix.
4. Launch languages and markets (affects date formats, units and translations).
5. Business model (free, paid, subscription); in-app billing would add a Play Billing dependency.
