# Vendored dependencies

Three dependencies are checked directly into the repository rather than resolved from a remote
repository: the native Matter (CHIP) binaries, the Google Home API Maven artifacts, and the
`ios-matter` Swift package. Cloning the repository and building is enough — none of them require
manual setup.

This page explains what each one is and what to do if you need to update it.

## Native Matter (CHIP) SDK binaries

[`/androidDeps/libs`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/androidDeps/libs)
contains prebuilt binaries checked directly into git. They are not built by this Gradle project.

| Kind | Files |
| --- | --- |
| Jars | `AndroidPlatform.jar`, `CHIPClusterID.jar`, `CHIPClusters.jar`, `CHIPController.jar`, `CHIPInteractionModel.jar`, `OnboardingPayload.jar`, `libMatterJson.jar`, `libMatterTlv.jar` |
| Native libraries | `libCHIPController.so` and `libc++_shared.so`, in [`/androidDeps/libs/jniLibs/arm64-v8a`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/androidDeps/libs/jniLibs/arm64-v8a) |

!!! note "Note"

    The native libraries are built for `arm64-v8a` only. There is no `x86_64` build, so these
    libraries will not load on an Android emulator — a physical arm64 device is required.

These binaries are built against **Matter 1.5.0**, as provided by Nordic. They come from Nordic's
fork of Project CHIP,
[`nrfconnect/sdk-connectedhomeip`](https://github.com/nrfconnect/sdk-connectedhomeip), the nRF
Connect SDK downstream of
[`project-chip/connectedhomeip`](https://github.com/project-chip/connectedhomeip), specifically its
Android `chip-tool` build target for arm64.

To rebuild them from source, follow the
[Android building instructions](https://github.com/nrfconnect/sdk-connectedhomeip/blob/9895b2bdb4c43b48426930f03e3c05502babd2f0/docs/platforms/android/android_building.md)
in that repository.

!!! note "Note"

    If you build the `.jar` and `.so` files yourself against a newer Matter version, this project may
    need changes to handle it. Newer Matter releases can add, rename, or change the behaviour of the
    APIs these binaries expose.

## Google Home API artifacts

The repository includes a
[`/mavenLocal`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/mavenLocal) directory
checked directly into git — a pre-built local Maven repository with the same directory structure and
artifact metadata (`maven-metadata.xml`, checksums) that Gradle expects. It is wired up in
`settings.gradle.kts`.

When you clone this repository and build, Gradle finds the Home API artifacts from `./mavenLocal`
transparently. No manual setup is required.

### What the directory contains

It vendors the following Android dependencies, which Google does not publish on public Maven
repositories:

| Artifact | Version | Purpose |
| --- | --- | --- |
| `com.google.android.gms:play-services-home` | `17.1.0` | The main Google Home Mobile SDK for Matter (the Home API). Provides API interfaces, device control, authorization, and commissioning services. |
| `com.google.android.gms:play-services-home-types` | `17.1.0` | A helper library containing models for device types, traits, command parameters, and other domain types. |

The POM of `play-services-home-types` declares a compile-scope dependency on `play-services-home`, so
**both artifacts must always be updated together**.

Google's public Maven repository (`google()`, that is `dl.google.com/android/maven2`) only publishes
`play-services-home` up to `16.0.0`, and does not publish `play-services-home-types` at all. Version
`17.1.0` introduced several new APIs that were not available in `16.0.0`.

### Updating to a newer version

!!! note "Not needed for a normal build"

    The `./mavenLocal` directory already ships the vendored `17.1.0` artifacts, so the steps below
    only matter if you are deliberately updating to a newer version.

The Google Home APIs are currently in **open beta**, which means they are available to developers but
may change without notice. They are not part of the standard Android SDK or the usual Google Play
Services libraries (`com.google.android.gms.*`), and they are not yet available in Maven Central or
Google's standard Maven repositories. Getting started therefore requires a few non-standard
integration steps:

1. Sign in to the [Google Cloud Console](https://console.cloud.google.com/) with your Google account.
1. Access the Home APIs early-access program and download the ZIP archive containing the SDK
   artifacts.
1. Extract the SDK into your system's local Maven repository, the `.m2/repository` directory:
    - **Linux:** `~/.m2/repository/`
    - **macOS:** `~/.m2/repository/`
    - **Windows:** `C:\Users\<User_Name>\.m2\repository\`
1. Add `mavenLocal()` to your Gradle `repositories` block so Gradle can find the artifacts.
   `settings.gradle.kts` already declares it alongside the vendored `./mavenLocal` repository.
1. Repeat this process each time the SDK is updated, until Google officially publishes it to a Maven
   repository.

!!! Caution "Breaking changes"

    The Home API is still evolving, so a newer version may introduce breaking changes. Check
    `androidDeps` and anywhere else the Home API is used — search for `play.services.home` in the
    source — and adjust as needed.

## ios-matter

[`/ios-matter`](https://github.com/nordicsemi/kotlin-nrf-matter/tree/main/ios-matter) is a full Swift
package — manifest and sources — checked directly into git, the Apple-side counterpart to the
vendoring described above. It used to be resolved from a git remote at an exact tag; it is now built
in place.

**It is not a SwiftPM dependency of the Kotlin build.** It is compiled to a static library and
consumed through plain cinterop, so the Swift object code ends up *inside* the published artifact.
Three Gradle tasks per iOS target do this, in `composeApp/build.gradle.kts`:

| Task | What it does |
| --- | --- |
| `compileIosMatterSwift<Target>` | Runs `xcodebuild` on `/ios-matter`, which also resolves and builds Pulse. |
| `iosMatterStaticLib<Target>` | Runs `libtool` on the resulting objects to produce `libios-matter.a`, and copies the Swift-generated Objective-C header and module map beside it. |
| `cinteropIosMatter<Target>` | Translates that module into the `iosMatter` Kotlin package and embeds the archive in the klib. |


`./gradlew :composeApp:iosMatterStaticLibs` builds the library for every target. All three tasks run
automatically as part of any iOS compile — there is nothing to invoke by hand.

Only the `@objc public` surface of `ios-matter` crosses the boundary; the Swift-generated
Objective-C header is the contract, which is why the Kotlin-facing classes are annotated. Kotlin
reaches them through the `iosMatter.*` package, for example `iosMatter.SwiftLogger` and
`iosMatter.LocalMatterLightController`.

### Why not localSwiftPackage

A SwiftPM declaration is published as `SwiftPMDependency.Local` carrying an **absolute** path —
inspect `matter-support-<version>-swiftpm-metadata.json` in any published artifact to see it. A
consumer resolving `matter-support` from Maven therefore cannot find the Swift code at all, and the
Swift sources are not in the klib either.

Only the version-pinned `swiftPackage(url = ...)` form is publishable, and that means a second source
of truth for the Swift code. Archiving the objects into the cinterop klib avoids both problems:
`no.nordicsemi.nrf.matter:matter-support` is self-contained, and Xcode needs no package graph.
Neither `iosApp` nor `nrfMatter` imports `ios_matter`; both reach it through Kotlin bridges such as
`KeychainKt.prepareKeychain()`.

### Editing it

Change a `.swift` file under `/ios-matter/ios-matter` and build. The task inputs cover the sources and
the manifest, so the library is rebuilt and re-archived automatically. There is no tag to push, no
version to bump, and no lockfile to realign.

Its own remote dependency, [Pulse](https://github.com/kean/Pulse), is still pinned by
`/ios-matter/Package.resolved` and is linked into the same archive.

One consequence of `/ios-matter` staying a local package: SwiftPM refuses `unsafeFlags` in a package
consumed as a dependency, but exempts local ones. That is what lets `/ios-matter/Package.swift` keep
`-enable-library-evolution`. Its comment explains why that flag is needed.
