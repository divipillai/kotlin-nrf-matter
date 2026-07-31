//
//  Device.swift
//  ios-matter
//
//  Created by Sylwester Zielinski on 28/07/2026.
//

import Matter

/// Metadata describing a commissioned Matter device.
///
/// Declared as an `@objc` class rather than a struct so it can cross the Objective-C bridge
/// into Kotlin/Native.
@objc public final class Device: NSObject {
    @objc public let deviceId: NSNumber
    @objc public let dateCommissioned: NSNumber
    @objc public let vendorId: String
    @objc public let producId: String
    @objc public let deviceType: NSNumber
    @objc public let name: String
    @objc public let productName: String
    @objc public let vendorName: String
    @objc public let uniqueId: String
    @objc public let softwareVersion: String
    @objc public let specificationVersion: NSNumber
    @objc public let serialNumber: String?
    @objc public let deviceMatterInfo: [DeviceMatterInfo]

    @objc public init(
        deviceId: NSNumber,
        dateCommissioned: NSNumber,
        vendorId: String,
        producId: String,
        deviceType: NSNumber,
        name: String,
        productName: String,
        vendorName: String,
        uniqueId: String,
        softwareVersion: String,
        specificationVersion: NSNumber,
        serialNumber: String?,
        deviceMatterInfo: [DeviceMatterInfo]
    ) {
        self.deviceId = deviceId
        self.dateCommissioned = dateCommissioned
        self.vendorId = vendorId
        self.producId = producId
        self.deviceType = deviceType
        self.name = name
        self.productName = productName
        self.vendorName = vendorName
        self.uniqueId = uniqueId
        self.softwareVersion = softwareVersion
        self.specificationVersion = specificationVersion
        self.serialNumber = serialNumber
        self.deviceMatterInfo = deviceMatterInfo
        super.init()
    }
}

/// Per-endpoint device types and cluster lists discovered on a Matter device.
@objc public final class DeviceMatterInfo: NSObject {
    @objc public let endpoint: NSNumber
    @objc public let types: [NSNumber]
    @objc public let serverClusters: [NSNumber]
    @objc public let clientClusters: [NSNumber]
    @objc public let manufacturerSpecificData: ManufacturerSpecificData?

    @objc public init(
        endpoint: NSNumber,
        types: [NSNumber],
        serverClusters: [NSNumber],
        clientClusters: [NSNumber],
        manufacturerSpecificData: ManufacturerSpecificData?
    ) {
        self.endpoint = endpoint
        self.types = types
        self.serverClusters = serverClusters
        self.clientClusters = clientClusters
        self.manufacturerSpecificData = manufacturerSpecificData
        super.init()
    }
}
