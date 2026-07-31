//
//  MatterDecommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 11/03/2026.
//

import Matter

/// Decommissions a device, removing it from the local fabric.
@objc public final class LocalMatterDecommissioner : NSObject {

    /// Removes the operational credentials fabric from the device and forgets it locally.
    ///
    /// - Parameter deviceId: The Matter node ID of the device to decommission.
    /// - Throws: An error if reading or removing the fabric fails.
    @objc public func decommission(deviceId: NSNumber) async throws {
        SwiftLogger.info("Decommission device: \(deviceId)")
        let controller = try! LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        
        SwiftLogger.debug("Erasing data on a remote device.")
        let baseDevice = MTRBaseDevice(nodeID: deviceId, controller: controller)
        let operationalCredentials = MTRBaseClusterOperationalCredentials(device: baseDevice, endpointID: 0, queue: .main)
        
        let fabrics = try await operationalCredentials!.readAttributeFabrics(with: nil)
        SwiftLogger.debug("Stored fabrics: \(fabrics)")
        let theFabric = fabrics[0] as! MTROperationalCredentialsClusterFabricDescriptorStruct
        
        let params = MTROperationalCredentialsClusterRemoveFabricParams()
        params.fabricIndex = theFabric.fabricIndex
        
        try await operationalCredentials!.removeFabric(with: params)

        SwiftLogger.debug("Removing device from local fabric.")
        controller.forgetDevice(withNodeID: deviceId)
        SwiftLogger.info("Decommission success")
    }
}
