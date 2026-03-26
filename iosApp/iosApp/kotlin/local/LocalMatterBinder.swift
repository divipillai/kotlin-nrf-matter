//
//  LocalMatterBinder.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 25/03/2026.
//

import ComposeApp
import Matter
import OSLog
import SharedCode

class LocalMatterBinder : MatterBinder {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "LocalMatterBinder")

    func bindSwitchToLight(switchNodeId: DeviceId, lightNodeId: DeviceId) async throws {
        logger.info("bindSwitchToLight")
        let controller = try LocalControllerProvider(logTag: "LocalMatterBinder").getController()!
        await grantAccessToSource(targetDeviceID: lightNodeId.nsNumber(), sourceNodeID: switchNodeId.nsNumber(), controller: controller)
        await bindSwitchToBulb(sourceDeviceID: switchNodeId.nsNumber(), sourceEndpoint: 1, targetNodeID: lightNodeId.nsNumber(), targetEndpoint: 1, clusterID: 6, controller: controller)
    }

    func grantAccessToSource(targetDeviceID: NSNumber, sourceNodeID: NSNumber, controller: MTRDeviceController) async {
        let targetDevice = MTRBaseDevice(nodeID: targetDeviceID, controller: controller)
        let aclCluster = MTRBaseClusterAccessControl(device: targetDevice, endpointID: 0, queue: .main)
        
        // Create an entry allowing the source node to 'Operate' this device
        let entry = MTRAccessControlClusterAccessControlEntryStruct()
        entry.privilege = 3 // Operate
        entry.authMode = 2  // CASE (Certificate-based)
        entry.subjects = [sourceNodeID]
        entry.targets = nil // nil means all clusters/endpoints on this node
        
        // Note: In production, you should read existing ACLs first and append this entry
        // to avoid overwriting the controller's own Admin access.
        do {
            try await aclCluster?.writeAttributeACL(withValue: [entry])
            logger.info("Access granted to node \(sourceNodeID)")
        } catch {
            logger.info("ACL write failed: \(error)")
        }
    }
    
    func bindSwitchToBulb(sourceDeviceID: NSNumber, sourceEndpoint: NSNumber, targetNodeID: NSNumber, targetEndpoint: NSNumber, clusterID: NSNumber, controller: MTRDeviceController) async {
        let sourceDevice = MTRBaseDevice(nodeID: sourceDeviceID, controller: controller)
        let bindingCluster = MTRBaseClusterBinding(device: sourceDevice, endpointID: sourceEndpoint, queue: .main)
        
        let bindingEntry = MTRBindingClusterTargetStruct()
        bindingEntry.node = targetNodeID
        bindingEntry.endpoint = targetEndpoint
        bindingEntry.cluster = clusterID // e.g., 6 for OnOff, 8 for LevelControl
        
        do {
            try await bindingCluster?.writeAttributeBinding(withValue: [bindingEntry])
            logger.info("Binding created successfully!")
        } catch {
            logger.info("Binding failed: \(error)")
        }
    }
}
