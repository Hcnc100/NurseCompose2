# Google Play closed testing

## Release checklist

### Workflow safety

Pushes to `feature/closed-testing-pipeline` run validation and generate the bundle; they do not publish to Google Play.
Manual workflow runs also do not publish by default. Publication requires explicitly enabling the `publish_to_play` input and configuring the Play service-account secret.
That optional upload targets **Internal testing**, not Closed testing. For Closed testing, use the generated AAB and the corresponding Play Console track.

1. Configure the four signing secrets used by `.github/workflows/closed-testing.yml`.
2. Run the workflow manually and upload the generated AAB to **Testing > Closed testing**.
3. Complete **App content > Health apps declaration** and the Data safety form.
4. Publish a privacy-policy URL that explains local storage, reminder data, photos, notifications, and PDF export.
5. Test notifications, exact alarms, full-screen alarms, alarm history (launch success/failure, registration, cancellation, deletion), Diagnostics export, camera, gallery, Room migration, dark mode, and PDF export on supported Android versions.
6. If the developer account is personal and was created after 13 November 2023, maintain at least 12 opted-in testers for 14 continuous days before requesting production access.

## Suggested store description

**NurseApp helps you record health measurements and organize medication reminders on your device.** Track temperature, blood glucose, blood pressure, and oxygen saturation with simple visual history. Create medication reminders with dose, schedule, optional photo, sound, vibration, or full-screen alarm behavior, and review a local activity log with dates, outcomes, registrations, cancellations, and deletions. Export selected reminders and measurements to a PDF for your own records.

The app is a personal record-keeping tool. It does not diagnose conditions, interpret results, replace professional medical advice, or connect to a medical device. Data is entered by the user. Review the generated PDF before sharing it and protect it as sensitive information.

## Important Play Console declarations

- Declare the app as a health-related app and accurately complete the Health apps declaration.
- Explain every sensitive-data purpose in the privacy policy and Data safety form.
- Do not claim that the app diagnoses, treats, prevents, or monitors a disease unless the required regulatory and policy evidence exists.
- Explain why notifications, exact alarms, vibration, full-screen intents, camera, and photos are requested.
- Use screenshots that match the current UI and do not show fake medical results.
- Provide reviewer instructions and a test account only if a login is introduced; currently the app is local-first.
