// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "no_nordicsemi_nrf_matter_composeApp_1_0_0",
  platforms: [
    .iOS("26.0")
  ],
  products: [
    .library(
      name: "no_nordicsemi_nrf_matter_composeApp_1_0_0",
      type: .none,
      targets: ["no_nordicsemi_nrf_matter_composeApp_1_0_0"]
    )
  ],
  dependencies: [
    .package(
      url: "git@github.com:sylwester-zielinski/ios-matter.git",
      exact: "0.0.9"
    )
  ],
  targets: [
    .target(
      name: "no_nordicsemi_nrf_matter_composeApp_1_0_0",
      dependencies: [
        .product(
          name: "ios-matter",
          package: "ios-matter"
        )
      ]
    )
  ]
)
