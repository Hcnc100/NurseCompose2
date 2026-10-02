# Conditional reminder form

## Goal
Show only applicable reminder fields and animate transitions with Compose.

## Instructions
User requested conditional sections and Compose animations. Preserve draft values while switching modes; do not push/publish automatically. Use isolated Codex_ emulators only.

## Discoveries
- Duration must precede recurrence/end-date inputs so users choose the schedule before entering dependent values.
- A hidden interval must not validate/block a single dose. Valid draft/stored values remain preserved; an invalid hidden value uses a positive internal fallback, which cannot introduce recurrence because endAt equals startAt.
- Independent sound/vibration/full-screen settings apply to every duration and should remain available, not disappear with recurrence.

## Accomplished
- Moved duration choices immediately below first-dose time.
- Single dose hides recurrence interval and upcoming-repetition preview; only Date range shows final-date selection; One day alone shows its day-boundary explanation.
- Animated applicable sections with expand/shrink and fade transitions, 200 ms. Full-screen permission help is animated and still conditional on the mode/device permission.
- Draft state stays outside visibility containers. Switching duration clears field focus and dismisses the keyboard so hidden inputs do not retain active keyboard focus.
- Added behavioral tests proving single-dose saving ignores an invalid hidden interval, restoring recurrence retains the invalid draft and requires correction, and final-date/help visibility matches selected duration.
- Added screenshot/layout checks for switching Single dose/Date range in en/es, light/dark, 100%/200% fonts.
- API 33 editor + layout suite: 36 tests passed. Additional latest expanded layout run: 24 tests passed, capturing both conditional modes.
- API 34 final editor + layout suite: 36 tests passed after the permission-help animation change. Final test/lint/assembleDebug/assembleDebugAndroidTest succeeded.
- Inspected Spanish dark single-dose and English light date-range captures at 200%. Save remains separate; first/final dates and duration choices wrap naturally. Transient radio press ripples in captures are not multiple selected radios.
- Updated native UI-matrix script to scroll explicitly to the relocated interval and duration options. This updated native matrix script was not executed during this session; Compose tests were executed instead.

## Next Steps
- Verify/guard newly created single doses whose default first-dose time has already passed by Save; never silently move the user's dose time. Legacy past single-dose schedules should not be extended automatically.
- Reliable real TalkBack navigation/spoken feedback remains unapproved.
- Actual day-boundary alarm behavior, populated charts/reports, signing/configuration and remote CI; Android 15/16 last.
- No commit/push/publication performed.

## Relevant Files
- app/src/main/java/com/nullpointer/nourseCompose/ui/screens/medication/MedicationScreen.kt — dependent sections, animation helper, validation and focus handling.
- app/src/androidTest/java/com/nullpointer/nourseCompose/medication/MedicationEditorUiTest.kt — hidden-field and duration-switch regressions.
- app/src/androidTest/java/com/nullpointer/nourseCompose/medication/MedicationCardLayoutTest.kt — conditional-mode large-text captures.
- tools/qa/Test-UiMatrix.ps1 — explicit scrolling for relocated fields.
- build/qa-conditional-form-captures — ignored PNG evidence.
- build/qa-conditional-complete-api34.log — final 36-test result.
