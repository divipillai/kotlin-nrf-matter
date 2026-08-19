# Releasing nRF Matter for Mobile

Both apps are released from `main`, from the same commit, using
[fastlane](https://fastlane.tools/) driven by manually triggered GitHub Actions
workflows. The workflows need repository secrets, so they can only be run by
maintainers, on this repository.

## Versioning

Version numbers for both platforms are derived from git tags, so the tag has to
exist before the build is made:

1. Tag the commit on `main` and push the tag.
2. Create the GitHub release for the tag.
3. Run the deploy workflow.

The workflows check out with `fetch-depth: 0` so the tags are available to the
build. The resolved version is visible in the workflow log.

## Artifacts

| Platform | Artifact | Channel | Workflow | fastlane lane |
| --- | --- | --- | --- | --- |
| Android | `androidApp-release.aab` | Play Store, `internal` track | [`deploy-to-play-store.yml`](.github/workflows/deploy-to-play-store.yml) | `deployInternal` |
| Android | `androidApp-release.aab` | Play Store, `production` track | — (run locally) | `deploy` |
| iOS | `.ipa` | TestFlight | [`deploy-to-testflight.yml`](.github/workflows/deploy-to-testflight.yml) | `upload_testflight` |

Android lanes are in [`fastlane/Fastfile`](fastlane/Fastfile), iOS lanes in
[`iosApp/fastlane/Fastfile`](iosApp/fastlane/Fastfile).

Identifiers: `no.nordicsemi.nrf.matter` on Android; `com.nordicsemi.nrf.matter`
and `com.nordicsemi.nrf.matter.extension` (the `MatterSupport` extension) on iOS.

## Before releasing

1. `main` is green and contains everything intended for the release.
2. Android builds and runs on a physical arm64 device — the vendored CHIP native
   libraries are `arm64-v8a` only, so the app does not run on an emulator:
   `./gradlew clean :androidApp:assembleDebug`.
3. iOS builds from [`iosApp`](iosApp) in Xcode 26 or newer.
4. Commissioning and control were tested on both platforms, with a Nordic DK or
   the Matter Virtual Device.
5. [`NOTICE`](NOTICE) still matches what ships — update it when a vendored binary,
   the Home API artifacts in `mavenLocal`, or an `ios-matter` dependency changes.
6. Store listing text and [screenshots](#screenshots) are up to date.

## Android

1. Tag and create the GitHub release.
2. Actions → **Deploy to Play Store Internal** → *Run workflow* on `main`.

The workflow builds the release bundle and uploads it to the `internal` track,
writing the service-account key and upload keystore from secrets and removing
them afterwards. Promote `internal` → `production` in the Play Console once
testing is done, or run `fastlane deploy` locally to upload straight to
`production`.

## iOS

The workflow publishes to TestFlight; the App Store release is made by hand from
that build.

1. Tag and create the GitHub release.
2. Actions → **Deploy to Testflight** → *Run workflow* on `main`.
3. Check the build in TestFlight.
4. In App Store Connect, create the App Store version, select the TestFlight
   build, attach the listing and screenshots, and submit for review.

The workflow runs on macOS, signs with
[match](https://docs.fastlane.tools/actions/match/) in read-only mode, and builds
the `iOS release` scheme, which compiles the shared Kotlin framework via
`:shared:embedAndSignAppleFrameworkForXcode`.

## Screenshots

Screenshots are updated manually in the Play Console and App Store Connect. The
GitHub Actions run cannot capture them, so the `screenshots` and
`upload_screenshots` lanes in [`iosApp/fastlane/Fastfile`](iosApp/fastlane/Fastfile)
are not part of the release workflows.

## Credentials

The workflows pass repository secrets to fastlane as environment variables. Their
meaning is documented by fastlane:

- App Store Connect API key — `APPLE_ISSUER_ID`, `APPLE_KEY_ID`, `APPLE_P8`
  (base64), used by
  [`app_store_connect_api_key`](https://docs.fastlane.tools/actions/app_store_connect_api_key/),
  plus `FASTLANE_USER`, `FASTLANE_PASSWORD`,
  `FASTLANE_APPLE_APPLICATION_SPECIFIC_PASSWORD` and
  `SPACESHIP_ONLY_ALLOW_INTERACTIVE_2FA`
  ([reference](https://docs.fastlane.tools/best-practices/continuous-integration/#authentication-with-apple-services)).
  Moreover, there are:
  `APP_STORE_CONNECT_API_KEY_ISSUER_ID`, `APP_STORE_CONNECT_API_KEY_KEY_ID` and
  `APP_STORE_CONNECT_API_KEY_KEY` - which are used by `pilot` and `deliver` commands.
- Code signing — `MATCH_PASSWORD`, `MATCH_GIT_PRIVATE_KEY`, `MATCH_KEYCHAIN_NAME`,
  `MATCH_KEYCHAIN_PASSWORD` ([match](https://docs.fastlane.tools/actions/match/)).
- Play Store — `PLAY_STORE_JSON_KEY`, written to `fastlane-api.json` as
  [`fastlane/Appfile`](fastlane/Appfile) expects, and the upload keystore:
  `KEYSTORE_B64`, `KEYSTORE_PSWD`, `KEYSTORE_ALIAS`, `KEYSTORE_KEY_PSWD`
  ([supply](https://docs.fastlane.tools/actions/supply/)).

## After releasing

Check that the Play Console and App Store Connect show the version from the tag,
and fill in the GitHub release notes. The shipped CHIP binaries are currently
built against Matter 1.5.0 (NCS v3.2.0 or newer).
