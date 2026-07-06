# nRF Matter for Mobile

A [Matter](https://csa-iot.org/all-solutions/matter/) commissioning and control companion app by
[Nordic Semiconductor](https://www.nordicsemi.com/), built with Kotlin Multiplatform and Compose Multiplatform
for Android and iOS.

The app lets you:
- **Commission** new Matter devices onto your fabric (via the Android Home API / Google Play Services).
- **Control** commissioned devices — door locks, lights, switches, and manufacturer-specific clusters.
- **Manage bindings** between devices, e.g. a switch controlling a light.
- **View logs** for diagnosing commissioning and cluster interactions.


## Project structure

This is a Kotlin Multiplatform project targeting Android and iOS.

* [`/composeApp`](./composeApp/src) — shared Compose Multiplatform UI, screens, view models, navigation, and DI
  (Koin). Contains the usual KMP source sets:
  - [`commonMain`](./composeApp/src/commonMain/kotlin) — code shared across all targets (screens for home,
    commissioning, bindings, logs, and per-device-type controllers for locks, lights, and switches).
  - `androidMain` / `iosMain` — platform-specific code, e.g. wiring up Matter commissioning on each platform.
* [`/androidDeps`](./androidDeps) — Android library wrapping the native Matter (CHIP) SDK and the
  Google Home API, exposing helpers such as `ChipClient`, `ClustersHelper`, and `BindingManager`.
* [`/core`](./core) — shared domain models (`Device`, `DeviceMatterInfo`, `LockDeviceState`, …) and a
  Room-backed logger used across platforms.
* [`/androidApp`](./androidApp) — the Android application entry point.
* [`/iosApp`](./iosApp/iosApp) — the iOS application entry point (SwiftUI host for the shared Compose UI).
  Even though the UI is shared, this project is required as the entry point for the iOS app, and is where
  you'd add any additional SwiftUI code.

### Build and run the Android application

Use the run configuration from the run widget in your IDE's toolbar, or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :androidApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :androidApp:assembleDebug
  ```

### Build and run the iOS application

Use the run configuration from the run widget in your IDE's toolbar, or open the [`/iosApp`](./iosApp)
directory in Xcode and run it from there.

## Requirements

- Android: minSdk 27+, a device with Google Play Services (Home API is used for commissioning).
- iOS: Xcode to build/run [`/iosApp`](./iosApp).

## License

Copyright (c) Nordic Semiconductor. Licensed under a BSD-3-Clause style license — see the license header in
[`App.kt`](./composeApp/src/commonMain/kotlin/no/nordicsemi/nrf/matter/App.kt) for full terms.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
and [Compose Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform.html).