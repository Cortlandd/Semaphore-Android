# Semaphore Data Safety Draft

Last updated: April 12, 2026

This is a working draft for Google Play Console based on the current Semaphore Android codebase and its included SDKs. It should be reviewed again whenever ads, billing, consent, analytics, or third-party SDK versions change.

## Current App Reality

Semaphore:

- does not require user account creation
- stores workout routines and workout progress locally on device
- uses Google AdMob banner ads in the free tier
- uses Google Play Billing for the one-time `remove_ads` purchase
- loads optional workout cover media from Klipy and caches selected media locally
- does not include a custom backend account system

## Recommended Top-Level Answers

### Does your app collect or share any of the required user data types?

Recommended answer: `Yes`

Reason:

- Google AdMob automatically collects and shares data for advertising, analytics, and fraud prevention.

Official source:

- https://developers.google.com/admob/android/privacy/play-data-disclosure

### Is all of the user data collected by your app encrypted in transit?

Recommended answer: `Yes`

Reason:

- Google states that data collected by the Google Mobile Ads SDK is encrypted in transit using TLS.
- Google Play Billing communication is handled through Google Play services.
- Network requests for remote media should also be kept on HTTPS endpoints.

### Do you provide a way for users to request that their data is deleted?

Recommended answer: `No`

Reason:

- Semaphore does not currently provide an account-based deletion workflow.
- Most app data is stored locally and can be removed by clearing app storage or uninstalling the app.
- If you want to answer `Yes` in the future, add a real deletion request flow and document it in the privacy policy.

## Recommended Data Type Selections

These recommendations are intentionally conservative.

### Approximate location

Recommended answer: `Collected` and `Shared`

Reason:

- AdMob collects IP address, which may be used to estimate the general location of a device.

Suggested purpose selections:

- Advertising or marketing
- Analytics
- Fraud prevention, security, and compliance

### App interactions

Recommended answer: `Collected` and `Shared`

Reason:

- AdMob states that it collects user product interactions, including app launch, taps, and video views.

Suggested purpose selections:

- Advertising or marketing
- Analytics
- Fraud prevention, security, and compliance

### Diagnostics

Recommended answer: `Collected` and `Shared`

Reason:

- AdMob states that it collects diagnostic information such as app launch time, hang rate, and energy usage.

Suggested purpose selections:

- Analytics
- Fraud prevention, security, and compliance

### Device or other IDs

Recommended answer: `Collected` and `Shared`

Reason:

- AdMob states that it collects device and account identifiers, including advertising ID and app set ID when available.

Suggested purpose selections:

- Advertising or marketing
- Analytics
- Fraud prevention, security, and compliance

## Recommended Answers for Other Common Data Types

Unless the app changes, these should generally remain `No`:

- name
- email address
- user IDs created by Semaphore
- phone number
- payment card details
- purchase history stored by Semaphore
- messages
- photos and videos uploaded by the user
- audio recordings
- files and documents from user storage
- precise location
- contacts
- health data from Health Connect or medical devices

## Notes About Purchases

The `remove_ads` entitlement should be treated as a Google Play purchase state, not as account data managed by Semaphore itself.

- Google Play Billing handles payment processing
- Semaphore does not directly process card details
- local purchase state should not be the source of truth for restoration

## Notes About Local Workout Data

Workout names, durations, streaks, completion counts, timestamps, and cached media are stored locally on device to power the app experience. As of this draft, that data does not appear to be collected by a custom Semaphore backend.

If that changes in the future, the Data safety form must be updated.

## Pre-Submission Review Checklist

Before submitting the form in Play Console:

1. Re-check the current AdMob disclosure page for the exact SDK version you ship.
2. Confirm whether you have enabled any additional ad or measurement features that require extra disclosures.
3. Confirm you are not transmitting workout data to a backend service.
4. Confirm the privacy policy language matches the final Play Console selections.
5. Revisit this form if you add crash reporting, analytics SDKs, sign-in, cloud sync, or Health Connect.

## Official References

- https://developers.google.com/admob/android/privacy/play-data-disclosure
- https://developer.android.com/privacy-and-security/declare-data-use
- https://support.google.com/googleplay/android-developer/answer/10787469
