# Reminder duration contract fix

## Goal
Make One day distinct from One dose without silently extending existing reminders.

## Instructions
Preserve persisted schedules unless the user explicitly changes duration. No schema migration or automatic push/publication. Only Codex_Alarm_API_34 was used for device tests.

## Discoveries
- Legacy equal startAt/endAt means one occurrence to ReminderSchedule. Relabeling those records as One dose preserves behavior.
- A calendar day is not always 24 hours; daylight-saving transitions need zone-aware next-day boundaries.
- Lint crashed internally in Kotlin FIR analysis of test sources during one run. A fresh no-daemon, single-worker run passed; no checks were suppressed and no configuration was changed.
- One combined API 34 run had a touch-injection failure on the flow's initial navigation. Targeted rerun passed; the flow test now explicitly waits for MainActivity window focus before injecting gestures.

## Accomplished
- New One day selections repeat at the chosen interval through the last millisecond of the first dose's local calendar day.
- Added explicit One dose only / Una sola toma. Legacy equal endpoints load in this mode; saving unchanged keeps their original endpoint.
- Added localized explanatory text: One day ends at the end of the first-dose day, not after an arbitrary 24-hour duration.
- The preview and saved entity use the same pure ReminderDuration calendar boundary helper. No existing records or alarm registrations are migrated automatically.
- Four unit regressions: repeated doses within the local day, both DST transitions (23/25 hours), late-night first dose, unchanged legacy single occurrence.
- Two editor regressions check the saved day boundary and unchanged legacy endpoint.
- Final unit debug suite: 33 tests, zero failures. test/lint/assembleDebug/assembleDebugAndroidTest passed on the fresh single-worker run.
- API 34: all 24 layout cases and 10 editor cases passed; create/edit/delete passed in the targeted 11-test rerun and again in the final isolated one-test run after adding focus synchronization. Do not describe the initial combined run as green: it had 1 failure out of 35.
- Inspected Spanish light 200% capture: all four duration choices, toolbar and Save fit. Screenshot evidence is under build/qa-duration-api34-captures.

## Next Steps
- Repeat duration changes on API 33 if needed before release; previous API 33 large-text results predate this duration change.
- Verify date/range extremes and successful actual One day scheduling, not just editor output and schedule-calculator tests.
- Reliable TalkBack navigation and spoken feedback remain unapproved.
- Populated chart/report consistency, release signing/configuration and remote CI, followed by Android 15/16 system images.
- Changes remain local, uncommitted and unpushed.

## Relevant Files
- app/src/main/java/com/nullpointer/nourseCompose/domain/medication/ReminderDuration.kt — local calendar cutoff.
- app/src/main/java/com/nullpointer/nourseCompose/ui/screens/medication/MedicationScreen.kt — distinct single-dose/day modes and shared preview/save semantics.
- app/src/main/res/values/strings.xml and values-es/strings.xml — localized options and explanation.
- app/src/test/java/com/nullpointer/nourseCompose/domain/medication/ReminderDurationTest.kt — pure scheduling boundaries.
- app/src/androidTest/java/com/nullpointer/nourseCompose/medication/MedicationEditorUiTest.kt — day/legacy output regressions.
- app/src/androidTest/java/com/nullpointer/nourseCompose/medication/MedicationReminderFlowTest.kt — focus-ready real persistence flow.
