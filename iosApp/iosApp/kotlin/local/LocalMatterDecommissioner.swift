//
//  MatterDecommissioner.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 11/03/2026.
//

import ComposeApp
import SharedCode
import Matter

/**
 * A helper class for decommissioning a device.
 * It is removed from local fabric after that.
 */
class LocalMatterDecommissioner : MatterDecommissioner {
    
    /**
     * Decommission a device and remove it from a local fabric.
     */
    func decommission(deviceId: DeviceId) async throws {
        SharedLogger.info("Decommission device: \(deviceId)")
        let controller = try! LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        
        SharedLogger.debug("Erasing data on a remote device.")
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)
        let operationalCredentials = MTRBaseClusterOperationalCredentials(device: baseDevice, endpointID: 0, queue: .main)
        
        let fabrics = try await operationalCredentials!.readAttributeFabrics(with: nil)
        SharedLogger.debug("Stored fabrics: \(fabrics)")
        let theFabric = fabrics[0] as! MTROperationalCredentialsClusterFabricDescriptorStruct
        
        let params = MTROperationalCredentialsClusterRemoveFabricParams()
        params.fabricIndex = theFabric.fabricIndex
        
        try await operationalCredentials!.removeFabric(with: params)

        SharedLogger.debug("Removing device from local fabric.")
        controller.forgetDevice(withNodeID: deviceId.nsNumber())
        SharedLogger.info("Decommission success")
    }
}
