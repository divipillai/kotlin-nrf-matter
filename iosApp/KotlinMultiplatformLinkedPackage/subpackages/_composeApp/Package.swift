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
      path: "../../../../ios-matter"
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
