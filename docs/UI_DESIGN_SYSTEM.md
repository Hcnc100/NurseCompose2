# NurseApp UI conventions

The Compose theme owns the complete light/dark Material color schemes and shape scale.
Do not introduce screen-local palettes or rely on unconfigured Material defaults.

- Brand accent: rose, darker in light mode and lighter in dark mode to retain readable text.
- Screen backgrounds: `background`; toolbars/navigation/cards: `surfaceContainer`.
- Main actions: `primary` / `onPrimary`; selected cards and navigation indicators:
  `primaryContainer` / `onPrimaryContainer`; destructive actions: `errorContainer` / `onErrorContainer`.
- Supporting text: `onSurfaceVariant`; success: `SuccessLight` / `SuccessDark`.
- Cards, primary buttons, FABs and fields share `MaterialTheme.shapes.medium` (16dp).
  Dialogs/sheets use `extraLarge` (24dp); circular switches/radio indicators retain native semantics.
- Back navigation uses `AppTopBar`. The Home toolbar/bottom navigation only appear on main tabs;
  child screens own their toolbar and system insets. The reminder editor reserves bottom space for Save.
- Keep charts' data-series colors separate from UI action colors; they encode measurement types.
- Keep the dedicated full-screen alarm's high-attention visual distinct from ordinary screens.

`AppThemeTest` checks text/action/container and success contrast in both modes (minimum 4.5:1).
Device QA: check all tabs, selected measurements, reminders/editor, logs, diagnostics, export and
settings in light/dark modes, with TalkBack, 200% font scale, keyboard open, and landscape.
Verify a single toolbar, no floating Save overlap, and readable selected/inactive navigation states.
