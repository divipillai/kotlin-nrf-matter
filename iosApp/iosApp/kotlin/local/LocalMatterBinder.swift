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
        logger.info("switchNodeId: \(switchNodeId.nsNumber())")
        logger.info("lightNodeId: \(lightNodeId.nsNumber())")
        let switchId = 30 as NSNumber
        let lightId = 31 as NSNumber
        let controller = try LocalControllerProvider(logTag: "LocalMatterBinder").getController()!
        logger.info("acl")
        await grantAccessToSource(targetDeviceID: lightId, sourceNodeID: switchId, controller: controller)
        logger.info("bind")
        await bindSwitchToBulb(sourceDeviceID: switchId, sourceEndpoint: 1, targetNodeID: lightId, targetEndpoint: 1, clusterID: 6, controller: controller)
    }

    func grantAccessToSource(targetDeviceID: NSNumber, sourceNodeID: NSNumber, controller: MTRDeviceController) async {
        let targetDevice = MTRBaseDevice(nodeID: targetDeviceID, controller: controller)
        let aclCluster = MTRBaseClusterAccessControl(device: targetDevice, endpointID: 0, queue: .main)
        
        let target = MTRAccessControlClusterAccessControlTargetStruct()
        target.cluster = NSNumber(value: 6) // On/Off cluster
        target.endpoint = nil
        target.deviceType = nil
        
        // Create an entry allowing the source node to 'Operate' this device
        let entry = MTRAccessControlClusterAccessControlEntryStruct()
        entry.privilege = 3 // Operate
        entry.authMode = 2  // CASE (Certificate-based)
        entry.subjects = [sourceNodeID]
        entry.targets = [target] // nil means all clusters/endpoints on this node
//        entry.fabricIndex = 1
        
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
        bindingEntry.fabricIndex = 1
        
        do {
            try await bindingCluster?.writeAttributeBinding(withValue: [bindingEntry])
            logger.info("Binding created successfully!")
        } catch {
            logger.info("Binding failed: \(error)")
        }
    }
}
