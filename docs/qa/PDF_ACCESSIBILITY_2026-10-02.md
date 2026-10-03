# Tagged PDF accessibility — 2026-10-02

## Goal
Improve report accessibility and evaluate libraries without changing the existing visual identity or using external services for health data.

## Instructions
- Preserve English/Spanish, measurement charts and exact values.
- Do not operate the user's physical device or original emulator. No push requested for these report changes.

## Decision and discoveries
- Selected `com.tom-roush:pdfbox-android:2.0.27.0` as an Android-compatible, Apache-2.0 PDF writer. Its upstream latest published release is January 2023: this is a maintenance risk to monitor, not a claim of current upstream PDFBox parity.
- iText offers high-level accessibility support but has AGPL/commercial licensing; it was not added. OpenPDF advertises Android support, but was not validated against this project's Android 21 minimum.
- Replaced Canvas-generated text with embedded Liberation Sans text. Canvas remains only for chart illustrations, exported as tagged figures with alternate descriptions; exact readings remain real text.
- PDFBox closes its save destination. A non-closing stream wrapper preserves caller ownership, covered by a regression test.
- Preserved upstream license/notice and Liberation font OFL in APK assets. Font name-table metadata verifies SIL OFL 1.1, Google 2010 / Red Hat 2012 copyright.

## Accomplished
- Language, document title and display-title preference.
- Document structure with hierarchical headings, paragraphs, figure descriptions, per-page marked content and reciprocal parent-tree references.
- Decorations and page numbers are artifacts, excluded from semantic reading order.
- Unicode mappings and embedded fonts; unsupported glyphs have a visible question-mark fallback while marked `ActualText` preserves the original text. This does NOT provide full visual support for emoji or all writing systems.
- Two original export instrumentation tests passed after migration on disposable Android 14. Both PDFBox and independent pypdf extraction recovered all 120 distinct medication IDs in both languages.
- Independent structural checks passed for four fixtures, including heading levels, alternate descriptions, embedded fonts, Unicode maps, tagged visible content and matching parent-tree/content links.
- Final debug APK/test assembly passed. Lint passed after migration and stream fix. All four instrumentation tests passed on disposable Android 14, including stream ownership and photo alternate descriptions.
- Independent checks passed again on final four PDFs: health exports have 65 marked-content links and four described figures each; medication exports have 1203/1204 content links and all 120 IDs. English/Spanish samples render correctly in Android PdfRenderer; Spanish chart page also reviewed with independent Poppler rendering. Disposable emulator stopped afterwards.

## Next steps / limitations
- Final rerun and white-background visual checks completed; no push performed.
- Not certified PDF/UA: no PDF/UA conformance metadata is claimed. A standards validator and manual reading/reflow with an accessible PDF viewer are still needed.
- TalkBack support depends on the viewer; merely opening a PDF does not verify semantic navigation. Manual physical-device feedback remains pending.
- Photo descriptions identify user-provided medication photos but cannot describe unverified image contents; user-authored photo descriptions would improve this.
- Charts have descriptive text and adjacent exact readings, not inferred medical interpretations. Raster chart graphics are 2x resolution; text remains selectable.
- Library size, Android 21 runtime, release/R8 and long-history performance warrant further checks before release. Production does not parse imported PDFs, but the older port still needs dependency monitoring.

## Sources
- PDFBox Android README and license: https://github.com/TomRoush/PdfBox-Android
- Release history: https://github.com/TomRoush/PdfBox-Android/releases
- Tagged-content API: https://github.com/TomRoush/PdfBox-Android/blob/master/library/src/main/java/com/tom_roush/pdfbox/pdmodel/PDPageContentStream.java
- iText accessibility: https://itextpdf.com/solutions/universal-accessibility-pdfua
- iText licensing: https://itextpdf.com/how-buy/AGPLv3-license
- OpenPDF: https://github.com/LibrePDF/OpenPDF

## Relevant files
- app/build.gradle.kts — pinned Android PDFBox dependency.
- app/src/main/java/com/nullpointer/nourseCompose/reports/ReportPdfWriter.kt — tagged writer and embedded text.
- app/src/main/java/com/nullpointer/nourseCompose/reports/MedicationReportExporter.kt — schedule and photo semantics.
- app/src/main/java/com/nullpointer/nourseCompose/reports/HealthDataPdfExporter.kt — measurement heading hierarchy.
- app/src/androidTest/java/com/nullpointer/nourseCompose/reports/ReportExporterTest.kt — export, extraction, structure, stream and photo checks.
- tools/qa/Test-PdfStructure.py — independent fixture validation, not certification.
- app/src/main/assets/licenses/ — distributed third-party license notices.
