// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_composeApp",
  platforms: [
    .iOS("26.0")
  ],
  products: [
    .library(
      name: "_composeApp",
      type: .none,
      targets: ["_composeApp"]
    )
  ],
  dependencies: [
    .package(
      url: "git@github.com:sylwester-zielinski/ios-matter.git",
      exact: "0.0.11"
    )
  ],
  targets: [
    .target(
      name: "_composeApp",
      dependencies: [
        .product(
          name: "ios-matter",
          package: "ios-matter"
        )
      ]
    )
  ]
)
