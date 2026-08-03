// swift-tools-version: 6.3
// The swift-tools-version declares the minimum version of Swift required to build this package.
//
// VENDORED COPY
//
// This is the ios-matter Swift package, kept in-tree rather than resolved from
// git@github.com:sylwester-zielinski/ios-matter.git. It was vendored from tag
// 0.0.20. Edit it here: a change is picked up by the next build with no tag, no
// push and no version bump. See "Vendored ios-matter Swift package" in the
// repository README.
//
// This manifest exists so xcodebuild can build the package (and resolve Pulse);
// it is NOT declared as a SwiftPM dependency of the Kotlin build. composeApp's
// `compileIosMatterSwift*`/`iosMatterStaticLib*` tasks archive the resulting
// objects into a static library that plain cinterop embeds in the klib, which is
// what lets the compiled Swift ship inside the published matter-support artifact.

import PackageDescription

let package = Package(
    name: "ios-matter",
    platforms: [
        // Matter and MatterSupport are only available on recent iOS releases.
        .iOS("26.0"),
    ],
    products: [
        // Products define the executables and libraries a package produces, making them visible to other packages.
        .library(
            name: "ios-matter",
            targets: ["ios-matter"]
        ),
    ],
    dependencies: [
        // Used by SharedLogger.
        .package(url: "https://github.com/kean/Pulse", from: "5.2.3"),
    ],
    targets: [
        // Targets are the basic building blocks of a package, defining a module or a test suite.
        // Targets can depend on other targets in this package and products from dependencies.
        // The sources live in `ios-matter/`, which is also what the Xcode project references.
        .target(
            name: "ios-matter",
            dependencies: [
                .product(name: "Pulse", package: "Pulse"),
            ],
            path: "ios-matter",
            swiftSettings: [
                // Keeps Pulse out of what consumers must resolve at compile time.
                //
                // SwiftLogger uses `internal import Pulse`. Without resilience the
                // emitted binary .swiftmodule still records that import, so every
                // client of `import ios_matter` has to resolve Pulse AND its Clang
                // submodule PulseObjCHelpers, failing with
                //   error: unable to resolve module dependency: 'PulseObjCHelpers'
                // unless the client happens to have them on its module search path.
                // With library evolution the internal import stops being a client
                // requirement.
                //
                // This does not remove Pulse from the package graph: it is still
                // resolved, built and linked, and still backs the log store at
                // runtime. It only stops leaking into consumers' compiles.
                //
                // `unsafeFlags` is the only way to set this in a SwiftPM manifest.
                // SwiftPM normally rejects unsafeFlags in a package consumed as a
                // dependency, but path-based (local) dependencies are exempt --
                // which is one fewer thing to worry about now that this package is
                // vendored instead of version-pinned.
                .unsafeFlags(["-enable-library-evolution"]),
            ]
        ),
    ],
    swiftLanguageModes: [.v5]
)
