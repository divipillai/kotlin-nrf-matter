# nRF Matter for Mobile

A [Matter](https://nrfconnectdocs.nordicsemi.com/ncs/latest/nrf/protocols/matter/index.html) commissioning and control companion app by
[Nordic Semiconductor](https://www.nordicsemi.com/), built with Kotlin Multiplatform and Compose
Multiplatform
for Android and iOS.

The app lets you:

- **Commission** new Matter devices onto your fabric:
    - Android — via the Android Home API / Google Play Services, onto a Google Home fabric.
    - iOS — via Apple's `MatterSupport` framework (`MatterAddDeviceRequest`), onto a local fabric
      managed
      directly by the app itself (using `Matter.framework` / `MTRDeviceController`), with a bundled
      app
      extension providing the system QR-code scanning UI.
- **Control** commissioned devices — door locks, lights, switches, and manufacturer-specific
  clusters.
- **Manage bindings** between devices, e.g. a switch controlling a light.
- **View logs** for diagnosing commissioning and cluster interactions.

## Project structure

This is a Kotlin Multiplatform project targeting Android and iOS.

* [`/composeApp`](./composeApp/src) — shared Compose Multiplatform UI, screens,
  navigation, and DI
  (Koin). Contains the usual KMP source sets:
    - [`commonMain`](./composeApp/src/commonMain/kotlin) — code shared across all targets (screens
      for home,
      commissioning, bindings, logs, and per-device-type controllers for locks, lights, and
      switches).
    - `androidMain` / `iosMain` — platform-specific code, e.g. wiring up Matter commissioning on
      each platform.
* [`/androidDeps`](./androidDeps) — Android library wrapping the native Matter (CHIP) SDK and the
  Google Home API, exposing helpers such as `ChipClient`, `ClustersHelper`, and `BindingManager`.
* [`/core`](./core) — shared domain models (`Device`, `DeviceMatterInfo`, `LockDeviceState`, …) and
  a
  Room-backed logger used across platforms.
* [`/androidApp`](./androidApp) — the Android application entry point.
* [`/iosApp`](./iosApp/iosApp) — the iOS application entry point (SwiftUI host for the shared
  Compose UI).
  Even though the UI is shared, this project is required as the entry point for the iOS app, and is
  where
  you'd add any additional SwiftUI code.

### `androidDeps` native Matter (CHIP) SDK binaries

[`/androidDeps/libs`](./androidDeps/libs) contains prebuilt binaries checked directly into git —
they are
**not** built by this Gradle project:

- Jars: `AndroidPlatform.jar`, `CHIPClusterID.jar`, `CHIPClusters.jar`, `CHIPController.jar`,
  `CHIPInteractionModel.jar`, `OnboardingPayload.jar`, `libMatterJson.jar`, `libMatterTlv.jar`.
- Native libraries: [`/androidDeps/libs/jniLibs/arm64-v8a`](./androidDeps/libs/jniLibs/arm64-v8a) —
  `libCHIPController.so` and `libc++_shared.so` (`arm64-v8a` only — there's no `x86_64` build, so
  these
  libs won't load on an Android emulator, only on a physical arm64 device).

They come from Nordic's fork of Project CHIP,
[`nrfconnect/sdk-connectedhomeip`](https://github.com/nrfconnect/sdk-connectedhomeip) (the NCS
downstream of
[project-chip/connectedhomeip](https://github.com/project-chip/connectedhomeip)) — specifically its
Android
`chip-tool` build target for arm64. To rebuild them from source:

1. Clone `sdk-connectedhomeip` and bootstrap the build environment (first time only):
   ```shell
   git clone https://github.com/nrfconnect/sdk-connectedhomeip.git
   cd sdk-connectedhomeip
   source scripts/bootstrap.sh
   ```
2. Point it at your Android SDK/NDK. This vendored build was produced with **NDK 28.2.13676358**, so
   use
   a matching version to keep the native ABI compatible:
   ```shell
   export ANDROID_HOME=~/Library/Android/sdk   # macOS; ~/Android/Sdk on Linux
   export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/28.2.13676358
   ```
3. Build the `android-arm64-chip-tool` target:
   ```shell
   ./scripts/build/build_examples.py --target android-arm64-chip-tool build
   ```
4. Copy the resulting artifacts into this repo:
    - `out/android-arm64-chip-tool/lib/*.jar` → [`androidDeps/libs`](./androidDeps/libs)
    - `out/android-arm64-chip-tool/lib/jni/arm64-v8a/*.so` → [
      `androidDeps/libs/jniLibs/arm64-v8a`](./androidDeps/libs/jniLibs/arm64-v8a)

Artifact names have changed across connectedhomeip versions (e.g. `OnboardingPayload.jar` was
previously
named `SetupPayloadParser.jar`), so pin to a commit/tag that matches what's currently vendored here
before
comparing filenames, and re-test commissioning/cluster control end-to-end after swapping in a new
build.

### `mavenLocal` — vendored Google Home API artifacts (Android only)

[`/mavenLocal`](./mavenLocal) is a flat-file Maven repository checked into git and wired up in
[`settings.gradle.kts`](./settings.gradle.kts) (`maven { url = uri("$rootDir/mavenLocal") }`, alongside
`mavenLocal()`). It exists to vendor Android dependencies Google doesn't publish on a public Maven repo:

- `com.google.android.gms:play-services-home` and `play-services-home-types`, both pinned at `17.1.0` —
  Google's Home Mobile SDK for Matter (the Home API), used by `androidDeps` to commission and control
  devices via Google Play Services.
- Google's public Maven repo (`google()` / `dl.google.com/android/maven2`) currently only publishes
  `play-services-home` up to `16.0.0` (`16.0.0-beta1`, `16.0.0`) and doesn't publish
  `play-services-home-types` at all, so the newer `17.1.0` build vendored here isn't something you can just
  `curl` — it comes from Google's restricted Home APIs early-access/partner channel.

To get a newer version:
1. Check the [Home Mobile SDK for Android](https://developers.home.google.com/matter/apis/home) page and
   Google's public Maven index first — if the version you need has since been published there, drop the
   vendored copy here and depend on `google()` directly instead.
2. Otherwise, download the newer artifacts from the Home APIs early-access program: sign in to the Google
   Cloud Console project you were given access to and grab the ZIP from the Home SDK storage bucket — see
   [Exploring the Android Google Home APIs SDK](https://proandroiddev.com/exploring-the-android-google-home-apis-sdk-72b29eef0819)
   for the walkthrough. Unzip it and install the `.aar`s into `./mavenLocal`, preserving the existing Maven
   layout (`com/google/android/gms/<artifact>/<version>/...`, including the `.pom` and checksums), e.g.:
   ```shell
   mvn install:install-file -Dmaven.repo.local=./mavenLocal \
     -DgroupId=com.google.android.gms -DartifactId=play-services-home -Dversion=<new-version> \
     -Dpackaging=aar -Dfile=<path-to-aar> -DpomFile=<path-to-pom>
   ```
   (repeat for `play-services-home-types`).
3. None of this is required just to build the project — `./mavenLocal` already ships the vendored
   `17.1.0` artifacts in this repo, so the steps above only matter if you're deliberately moving to a
   newer version. Re-test Android commissioning/control end-to-end after swapping versions.

   > **Warning:** the Home API is still evolving, so a newer version may introduce changes that weren't
   > present in the older one — check `androidDeps` and anywhere else the Home API is used, and adjust as
   > needed.

### Build and run the Android application

Use the run configuration from the run widget in your IDE's toolbar, or build it directly from the
terminal:

- on macOS/Linux
  ```shell
  ./gradlew :androidApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :androidApp:assembleDebug
  ```

### Build and run the iOS application

Use the run configuration from the run widget in your IDE's toolbar, or open the [
`/iosApp`](./iosApp)
directory in Xcode and run it from there.

## Requirements

- Android: minSdk 27+, a device with Google Play Services (Home API is used for commissioning).
- iOS: Xcode to build/run [`/iosApp`](./iosApp).

## Initial setup: hosting Thread network credentials

Commissioning a **Thread** Matter device (as opposed to Wi-Fi) requires a Thread Border Router
already
running on the local network, and Thread network credentials available on the phone — see the
comment in
[`LocalMatterCommissioner.swift`](./iosApp/iosApp/kotlin/local/LocalMatterCommissioner.swift).
There's no
code in this repo for setting this up: it's provided by OS-level home hub infrastructure, configured
once
per network before you use this app.

- **iOS** — set up a Thread-capable home hub in Apple's Home app: a HomePod mini, HomePod, or Apple
  TV 4K
  (Wi-Fi + Ethernet model — the Wi-Fi-only Apple TV 4K does *not* support Thread) added to a room.
  HomePod
  (mini) becomes a home hub automatically; Apple TV becomes one once assigned to a room. Once set
  up, Thread
  credentials are stored on the phone and Apple's `MatterSupport` framework (
  `MatterAddDeviceRequest`)
  surfaces them automatically to this app during commissioning — no extra configuration in-app.
- **Android** — set up a Thread Border Router such as a Nest Hub (2nd gen) or Google TV Streamer (
  4K) via
  the Google Home app. Google Play Services (the Home API) then makes the credentials available to
  this
  app the same way.
- Either way:
    - The phone and the hub **must be on the same Wi-Fi network** — credential/device discovery
      relies on
      local-network multicast (mDNS), which doesn't cross subnets or routers.
    - The hub needs a **user account signed in** (a Google account added via the Google Home app, or
      an
      Apple ID signed into the Home app) before it will share any credentials — a freshly unboxed,
      no-account
      hub won't work.
    - Make sure the router on the network has **IPv6 enabled** — without it, Thread commissioning
      can appear
      to succeed but device control will fail afterwards.
- Matter standardizes Thread credential sharing across ecosystems, so a single hub can plausibly
  serve both
  platforms — e.g. a Google TV Streamer (4K) set up once in Google Home has been observed working
  for both
  iOS and Android commissioning in this app, without a separate Apple-ecosystem hub. Treat this as a
  field observation rather than a guarantee — re-verify if you hit setup issues on one platform.

### Testing without a hub: Matter Virtual Device (MVD)

If you don't have a Thread Border Router or physical accessory handy, Google's
[Matter Virtual Device](https://developers.home.google.com/matter/tools/virtual-device) (MVD) tool
lets you
commission a simulated Matter accessory from a Mac instead:

1. Download the MVD `.dmg` for your Mac (Apple Silicon or Intel) and drag it into `Applications`.
2. Launch MVD and configure the simulated accessory (device type, name, discriminator, Matter port,
   test VID/PID).
3. Commission it from this app like a real device — it shows a QR code and joins over the macOS
   existing Wi-Fi connection. (Make sure that Google Home app is installed to commission the device in the
   Android platform).
4. The Mac running MVD and the phone **must be on the same Wi-Fi network**, for the same
   mDNS-discovery
   reason as above.
5. Once commissioned, you can control the simulated device from this app, but not all features such as light
   switch binding is not available with MVD.

## License

Copyright (c) Nordic Semiconductor. Licensed under a BSD-3-Clause style license — see the license
header in
[`App.kt`](./composeApp/src/commonMain/kotlin/no/nordicsemi/nrf/matter/App.kt) for full terms.

---

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
and [Compose Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html).