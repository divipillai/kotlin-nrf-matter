# Project structure

This is a Kotlin Multiplatform project targeting Android and iOS.

| Module | Contents |
| --- | --- |
| [`/composeApp`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/composeApp/src) | Shared Compose Multiplatform UI, screens, navigation, and dependency injection (Koin). |
| [`/shared`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/shared) | A thin Kotlin Multiplatform module that produces the iOS framework the Xcode project consumes. |
| [`/androidDeps`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/androidDeps) | Android library wrapping the native Matter (CHIP) SDK and the Google Home API. |
| [`/core`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/core) | Shared domain models and the logging abstraction. |
| [`/androidApp`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/androidApp) | The Android application entry point. |
| [`/iosApp`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/iosApp/iosApp) | The iOS application entry point and the commissioning app extension. |
| [`/ios-matter`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/ios-matter) | The vendored Swift package wrapping Apple's Matter frameworks. |

## composeApp

[`/composeApp`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/composeApp/src) contains
the shared Compose Multiplatform UI, screens, navigation, and dependency injection (Koin), in the
usual Kotlin Multiplatform source sets:

- [`commonMain`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/composeApp/src/commonMain/kotlin)
  — code shared across all targets: screens for home, commissioning, bindings, and logs, plus the
  per-device-type controllers for locks, lights, and switches.
- `androidMain` and `iosMain` — platform-specific code, for example wiring up Matter commissioning on
  each platform.

## shared

[`/shared`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/shared) is a thin Kotlin
Multiplatform module that `api`/`export`s `:composeApp` and produces the iOS framework the Xcode
project consumes.

It carries no source of its own; it exists so that Swift has a single `import shared` to reach the
Kotlin surface. Both Xcode targets build it through a run-script phase calling
`./gradlew :shared:embedAndSignAppleFrameworkForXcode`.

## androidDeps

[`/androidDeps`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/androidDeps) is an Android
library wrapping the native Matter (CHIP) SDK and the Google Home API, exposing helpers such as
`ChipClient`, `ClustersHelper`, and `BindingControllerImpl`.

It depends on prebuilt binaries and vendored Maven artifacts — see
[Vendored dependencies](vendored_dependencies.md).

## core

[`/core`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/core) holds the shared domain
models (`Device`, `DeviceMatterInfo`, `LockDeviceState`, and others) and the `NordicLogger`
abstraction used across platforms — backed by Room on Android and, on iOS, by the Pulse-based
`SwiftLogger` from `ios-matter`.

## androidApp

[`/androidApp`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/androidApp) is the Android
application entry point.

## iosApp

[`/iosApp`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/iosApp/iosApp) is the iOS
application entry point — a SwiftUI host for the shared Compose UI — plus the `nrfMatter` target,
which is the `MatterSupport` app extension that provides the system commissioning and QR-code UI.

Even though the UI is shared, this project is required as the entry point for the iOS app, and is
where you would add any additional SwiftUI code.

## ios-matter

[`/ios-matter`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/ios-matter) is the Swift
package that wraps Apple's `Matter` and `MatterSupport` frameworks, vendored into the repository
rather than resolved from git. `:composeApp` runs cinterop against it, so this is where the iOS half
of commissioning, cluster access, and the keypair and storage shared with the Matter extension lives.

See [Vendored dependencies](vendored_dependencies.md#ios-matter) for how it is built and consumed.
