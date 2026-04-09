# Semaphore Android

Semaphore is a lightweight Android workout timer that lets you build a list of timed intervals, attach optional GIF references, and run the full routine from start to finish.

## Development

Build the debug app:

```sh
bash ./gradlew assembleDebug
```

Run unit tests:

```sh
bash ./gradlew testDebugUnitTest
```

## Versioning

Semaphore currently stores its release version in [`app/build.gradle.kts`](/Users/cortland/development/android/Semaphore-Android/app/build.gradle.kts) inside `defaultConfig`:

- `versionCode`
- `versionName`

When preparing a release:

1. Increase `versionCode` to a value higher than the last Play Store release.
2. Update `versionName` to the user-facing version string you want to ship.
3. Build the app and confirm Settings shows the expected version string.

The Settings screen displays the packaged app version as:

```text
versionName (versionCode)
```

Example:

```text
1.2.0 (14)
```

## GitHub Release Flow

Suggested release flow:

1. Commit the version bump.
2. Merge the release commit to your main branch.
3. Create a Git tag that matches the release version.

Example:

```sh
git tag android-v1.2.0
git push origin android-v1.2.0
```

4. Create the GitHub release from that tag.
5. In the release notes, include:
   - The shipped `versionName`
   - The shipped `versionCode`
   - Major user-facing changes
   - Any migration notes or monetization changes

## Google Play Release Flow

Before uploading:

1. Confirm `versionCode` is higher than the last production build.
2. Confirm `versionName` matches the release notes and GitHub tag.
3. Build your release artifact from the version-bumped commit.
4. Upload the artifact to the correct Play Console track.
5. Copy the same high-level release notes into the Play release notes field.

Recommended checklist:

1. Verify ads use production IDs for release builds.
2. Verify the `remove_ads` in-app product exists in Play Console and is active.
3. Verify the support email and FAQ content are current.
4. Verify the Settings version matches the intended store release.

## Monetization

Semaphore supports:

- Banner ads
- A one-time `remove_ads` purchase

Before shipping to production, set real values for:

- `ADMOB_APP_ID`
- `ADMOB_BANNER_AD_UNIT_ID`

The project currently falls back to Google test IDs when those Gradle properties are not supplied.

## Things to Know

- Semaphore uses a foreground service for workout timer playback so timers can continue running while the app is backgrounded or closed.
- The app declares the foreground service as `specialUse` in the Android manifest.
- Before shipping to Google Play, you must also declare the matching foreground service use in Play Console under `Policy > App content`.
- The Play Console declaration should explain that Semaphore uses a foreground service to keep workout timers active and visible to the user through an ongoing notification, including timer transitions.
