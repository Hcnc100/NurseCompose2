# Internal testing workflow restored

## Goal
Restore the user's intended automatic Internal testing delivery after push.

## Instructions
The user explicitly wants the pipeline to publish to Google Play Internal testing for testing on physical devices. Internal testing must not be confused with Production publication.

## Discoveries
- The manual publication gate introduced in c4cb246 contradicted this intended workflow. The prior automatic Internal upload was correct for this project.

## Accomplished
- Removed the publish_to_play manual input and restored secret-dependent Play decoding/upload conditions.
- Existing editor-regression prerequisites still gate bundle generation and upload.
- Upload remains track=internal; no Production/Closed-track publishing was added.
- Corrected workflow documentation. This decision supersedes the publication-gating section of PREPUSH_VALIDATION_2026-10-02.md.

## Next Steps
- Confirm remote CI and Internal upload outcome; local source checks alone do not confirm Play delivery.
- Continue device/TalkBack/release QA already documented.

## Relevant Files
- .github/workflows/closed-testing.yml — restored automatic Internal delivery.
- docs/google-play-closed-testing.md — intended workflow and track distinction.
