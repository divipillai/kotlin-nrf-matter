// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("26.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/no_nordicsemi_nrf_matter_composeApp_1_0_0")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "no_nordicsemi_nrf_matter_composeApp_1_0_0", package: "no_nordicsemi_nrf_matter_composeApp_1_0_0")
      ]
    )
  ]
)
