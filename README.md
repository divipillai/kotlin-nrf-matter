# nRF Matter for Mobile

A [Matter](https://nrfconnectdocs.nordicsemi.com/ncs/latest/nrf/protocols/matter/index.html)
commissioning and control companion app by
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
they are not built by this Gradle project:

- Jars: `AndroidPlatform.jar`, `CHIPClusterID.jar`, `CHIPClusters.jar`, `CHIPController.jar`,
  `CHIPInteractionModel.jar`, `OnboardingPayload.jar`, `libMatterJson.jar`, `libMatterTlv.jar`.
- Native libraries: [`/androidDeps/libs/jniLibs/arm64-v8a`](./androidDeps/libs/jniLibs/arm64-v8a) —
  `libCHIPController.so` and `libc++_shared.so` (`arm64-v8a` only — there's no `x86_64` build, so
  these
  libs won't load on an Android emulator, only on a physical arm64 device).

These binaries are built against **Matter 1.5.0**, as provided by Nordic. It comes from Nordic's
fork of Project CHIP,
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

> **Note:** if you build  `.jars`/`.so` files yourself against a newer Matter version, this project
> may need some changes to handle the newer version — newer Matter releases can add, rename, or
> change the behavior of
> the APIs these binaries expose.

### `mavenLocal` — vendored Google Home API artifacts (Android only)

This project includes a `./mavenLocal` directory checked directly into git — a pre-built local Maven
repository
with the same directory structure and artifact metadata (`maven-metadata.xml`, checksums) that
Gradle expects.
It is wired up in [`settings.gradle.kts`](./settings.gradle.kts).

When you clone this repo and build, Gradle finds the Home API artifacts from `./mavenLocal`
transparently
— no manual setup required.

#### What `./mavenLocal` contains

The directory vendors the following Android dependencies Google doesn't publish on public Maven
repos:

- **`com.google.android.gms:play-services-home`** at `17.1.0` — the main Google Home Mobile SDK for
  Matter (the Home API). Provides API interfaces, device control, authorization, and commissioning
  services.
- **`com.google.android.gms:play-services-home-types`** at `17.1.0` — a helper library containing
  models
  for device types, traits, command parameters, and other domain types. Its POM declares a
  compile-scope
  dependency on `play-services-home`, so **both artifacts must always be updated together**.

Google's public Maven repo (`google()` / `dl.google.com/android/maven2`) only publishes
`play-services-home`
up to `16.0.0` and doesn't publish `play-services-home-types` at all. Version `17.1.0` introduced
several new
APIs that weren't available in `16.0.0`

#### Gradle setup and availability of Google Home APIs for Android

> **Note:** This is not required just to build the project — `./mavenLocal` folder already ships the
> vendored `17.1.0` artifacts in this repo, so the steps below only matter if you're deliberately
> updating to a newer version.
>
The Google Home APIs are currently in **open beta**, which means they are available to developers,
but they may
change without notice. They are **not** part of the standard Android SDK or the usual Google Play
Services
libraries (`com.google.android.gms.*`), and they are **not yet available** in Maven Central or
Google's
standard Maven repositories (`google()` / `dl.google.com/android/maven2`).
Therefore, getting started requires a few non-standard integration process.

#### How to get the SDK: manual download

1. Sign in to the [Google Cloud Console](https://console.cloud.google.com/) with your Google
   account.
2. Access the Home APIs early-access program and download the ZIP archive containing the SDK
   artifacts.
3. Extract the SDK into your system's local Maven repository, `.m2/repository` directory. This is
   the standard path used for local Maven repositories.
    - **Linux:** `~/.m2/repository/`
    - **macOS:** `~/Users/<User_Name>/.m2/repository/`
    - **Windows:** `C:\Users\<User_Name>\.m2\repository\`
4. Add `mavenLocal()` to their Gradle `repositories` block so Gradle can find the artifacts.
5. Repeat this process each time the SDK is updated, until Google officially publishes it to a Maven
   repository.

> **Warning:** the Home API is still evolving, so a newer version may introduce breaking changes —
> check `androidDeps` and anywhere else the Home API is used (search for `play.services.home` in the
> source), and adjust as needed.
>

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

## Firmware supported

The vendored CHIP binaries (see [
`androidDeps` native Matter (CHIP) SDK binaries](#androiddeps-native-matter-chip-sdk-binaries))
are built against **Matter 1.5.0**, first introduced in **nRF Connect SDK v3.2.0**, so below listed
Nordic DK running Matter firmware built with NCS v3.2.0 or
newer should be compatible for testing commissioning/control with this app.

| Development Kit | SoC       |
|-----------------|-----------|
| nRF52840 DK     | nRF52840  |
| nRF5340 DK      | nRF5340   |
| nRF54L15 DK     | nRF54L15  |
| nRF54LM20 DK    | nRF54LM20 |

For the authoritative, up-to-date list of supported hardware, see Nordic's
[Matter hardware and memory requirements](https://nrfconnectdocs.nordicsemi.com/ncs/latest/nrf/protocols/matter/getting_started/hw_requirements.html)
page — new DKs and SoCs are added there as they gain Matter support.

## Initial setup: hosting Thread network credentials

Commissioning a **Thread** Matter device requires a Thread Border Router already running on the
local
network, and Thread network credentials available on the phone. Setup code is not part of this
repository — it relies on the OS-provided home hub infrastructure, which is configured once per
network
before the app is used.

### iOS

The iPhone needs a Thread Border Router on the same network, and the corresponding Thread Network
Credentials already installed via the system — these are installed through a system API and shared
with
every app on the phone, this one included. There are two ways to get them onto the iPhone:

- **Apple's own Home app**: set up a Thread-capable home hub — a HomePod mini, HomePod, or Apple TV
  4K
  (Wi-Fi + Ethernet model only; the Wi-Fi-only Apple TV 4K does *not* support Thread) — added to a
  room.
  HomePod (mini) becomes a home hub automatically; Apple TV becomes one once assigned to a room.
  Once set
  up, credentials are stored on the phone and Apple's `MatterSupport` framework
  (`MatterAddDeviceRequest`) surface them to this app automatically during commissioning — no extra
  in-app configuration.
- **A third-party ecosystem's companion app**, when the Thread network is instead provided by a
  device
  such as a Samsung TV or a dedicated hub like a **Google TV Streamer 4K**: sign in to the
  manufacturer's
  companion app, add the Thread-enabled device to the home, and enable its Thread Border Router
  functionality. For example:
    - **Samsung**: [SmartThings](https://apps.apple.com/us/app/smartthings/id1222822904)
    - **Google**: [Google Home](https://apps.apple.com/us/app/google-home/id680819774)

  For detailed instructions, refer to the device manufacturer's documentation. If the credentials
  aren't immediately available, commissioning a Matter device via the corresponding companion app
  may
  trigger their download and installation.

  > **Note**
  >
  > This app has been tested with a **Google TV Streamer 4K**. At the time of writing, Google
  provides
  > no alternative method for installing or sharing Thread Network Credentials on iPhone other than
  > through the **Google Home** app.

### Android

Set up a Thread Border Router — such as a Nest Hub (2nd gen) or Google TV Streamer 4K — via the
Google
Home app. Google Play Services (the Home API) then makes the credentials available to this app the
same
way.

### Either way

- The phone and the hub **must be on the same Wi-Fi network** — credential/device discovery relies
  on
  local-network multicast (mDNS), which doesn't cross subnets or routers.
- The hub needs a **user account signed in** (a Google account added via the Google Home app, or an
  Apple ID signed in to the Home app) before it will share any credentials — a freshly unboxed,
  no-account hub won't work.
- Make sure the router on the network has **IPv6 enabled** — without it, Thread commissioning can
  appear to succeed but device control might fail afterward.
- Matter standardizes Thread credential sharing across ecosystems, so a single hub can plausibly
  serve
  both platforms — e.g. a Google TV Streamer 4K set up once in Google Home has been observed working
  for both iOS and Android commissioning in this app, without a separate Apple-ecosystem hub. Treat
  this as a field observation rather than a guarantee — re-verify if you hit setup issues on one
  platform.

### Testing without a hub: Matter Virtual Device (MVD)

If you don't have a Thread Border Router or physical accessory handy, Google's
[Matter Virtual Device](https://developers.home.google.com/matter/tools/virtual-device) (MVD) tool
lets you
commission a simulated Matter accessory from a Mac instead:

1. Download the MVD `.dmg` for your Mac (Apple Silicon or Intel) and drag it into `Applications`.
2. Launch MVD and configure the simulated accessory (device type, name, discriminator, Matter port,
   test VID/PID).
3. Commission it from this app like a real device — it shows a QR code and joins over the macOS
   existing Wi-Fi connection. (Make sure that Google Home app is installed to commission the device
   in the
   Android platform).
4. The Mac running MVD and the phone **must be on the same Wi-Fi network**, for the same
   mDNS-discovery
   reason as above.
5. Once commissioned, you can control the simulated device from this app, but not all features such
   as light
   switch binding is not available with MVD.

## License

Copyright (c) Nordic Semiconductor. Licensed under a BSD-3-Clause style license — see the license
header in
[`App.kt`](./composeApp/src/commonMain/kotlin/no/nordicsemi/nrf/matter/App.kt) for full terms.

---

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
and [Compose Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html).