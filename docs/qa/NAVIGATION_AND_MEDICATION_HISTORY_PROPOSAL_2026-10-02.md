# Navigation and medication-history proposal

## Goal
Separate everyday patient-facing navigation from technical diagnostics and uncommon data actions. Proposal only; no app behavior changed in this review.

## Instructions
- Do not perform TalkBack testing of exported PDFs (user's latest explicit constraint).
- Preserve the sidebar and established rose/light/dark design.
- Keep diagnostic export useful to the developer under Settings.

## Verified discoveries
- The drawer renders all DrawerActions in enum order with equal visual priority. Import/export precede history; deletion is exposed alongside ordinary destinations.
- Its header currently occupies 150 dp. PDF export reuses the import/backup icon; Settings uses a wrench; cloud imagery implies a service that is not established by this menu.
- AlarmLogEntity stores reminder identity/name, event type/time, success and technical details. AlarmLogEvent has launch/dismiss/snooze and lifecycle events, but no explicit medication-taken confirmation. Dismissal must not be interpreted as intake.

## Proposed information architecture
- Keep the drawer: Medication history (history/checklist icon), Reports (document/chart icon), Settings (gear icon).
- Reports offers health-data and medication PDFs using existing export capabilities; clarify scope before sharing. Never label PDF sharing as a restorable backup.
- Settings: Appearance and language; Reminders and permissions; Data and files; Help and support.
- Move Import records and Export data under Data and files, retaining accurate descriptions of formats and scope. Do not promise whole-app restore unless implemented.
- Move Delete all data to the bottom of Data and files, with destructive styling, explicit scope and confirmation. Keep ordinary navigation unaffected.
- Keep Diagnosis and support, technical alarm log and Export diagnostics under Help and support. Label technical information clearly, with privacy warning before sharing. Do not hide support behind secret gestures or make it debug-build-only.
- Reduce branding header to roughly 88–104 dp, retain logo and app name, use consistent outline icons and theme colors in both modes. Avoid cloud icons for local files.

## Patient-facing history
- Date range presets: Today, Last 7 days, Custom; medication filter; chronology grouped by day.
- Card: medication/dose, scheduled occurrence, recorded alarm event, patient-reported confirmation and confirmation timestamp.
- Neutral state: Not confirmed, NOT Missed or Not taken merely because no confirmation exists. Snoozed/dismissed describe alarm interaction only.
- Explicit future intake actions: Mark as taken and Mark as not taken, with undo/correction. These require a new persisted occurrence-based model, migration and tests; existing event logs cannot supply historical intake truth.
- Keep schedule start/end separate from actual confirmed intake dates. Do not fabricate older occurrences from the current reminder schedule or present missing logs as evidence of failure.

## Accomplished
- Reviewed screenshot, drawer/header source and alarm-event schema.
- Prepared naming, placement and icon proposal; no navigation, persistence or PDF testing changes executed.

## Next steps
1. Implement drawer/Settings reorganization using existing actions, retaining all capabilities.
2. Build a simpler event-history presentation from recorded events with clear evidence limits.
3. Design explicit occurrence/confirmation storage before adding taken/not-taken UX; validate en/es, both themes, accessibility and migrations.

## Relevant files
- app/src/main/java/com/nullpointer/nourseCompose/ui/screens/home/widgets/DrawerContent.kt
- app/src/main/java/com/nullpointer/nourseCompose/ui/screens/home/widgets/ContainerDrawer.kt
- app/src/main/java/com/nullpointer/nourseCompose/ui/screens/home/actions/DrawerActions.kt
- app/src/main/java/com/nullpointer/nourseCompose/models/entity/AlarmLogEntity.kt
- app/src/main/java/com/nullpointer/nourseCompose/domain/alarm/AlarmLogEvent.kt

## Reference
Material navigation component anatomy (labels, icons and destination hierarchy): https://github.com/material-components/material-components-android/blob/master/docs/components/NavigationDrawer.md
The current Expressive guidance favors expanded rails; this proposal preserves the user's explicitly requested sidebar rather than introducing an unrelated navigation migration.
