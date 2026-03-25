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
        let controller = try LocalControllerProvider(logTag: "LocalMatterBinder").getController()!
        grantAccessToSource(targetDeviceID: lightNodeId.nsNumber(), sourceNodeID: switchNodeId.nsNumber(), controller: controller)
    }

    func grantAccessToSource(targetDeviceID: NSNumber, sourceNodeID: NSNumber, controller: MTRDeviceController) {
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
        aclCluster?.writeAttributeACL(withValue: [entry]) { error in
            if let error = error {
                print("ACL write failed: \(error)")
            } else {
                print("Access granted to node \(sourceNodeID)")
            }
        }
    }
    
    func bindSwitchToBulb(sourceDeviceID: NSNumber, sourceEndpoint: NSNumber, targetNodeID: NSNumber, targetEndpoint: NSNumber, clusterID: NSNumber, controller: MTRDeviceController) {
        let sourceDevice = MTRBaseDevice(nodeID: sourceDeviceID, controller: controller)
        let bindingCluster = MTRBaseClusterBinding(device: sourceDevice, endpointID: sourceEndpoint, queue: .main)
        
        let bindingEntry = MTRBindingClusterTargetStruct()
        bindingEntry.node = targetNodeID
        bindingEntry.endpoint = targetEndpoint
        bindingEntry.cluster = clusterID // e.g., 6 for OnOff, 8 for LevelControl
        
        bindingCluster?.writeAttributeBinding(withValue: [bindingEntry]) { error in
            if let error = error {
                print("Binding failed: \(error)")
            } else {
                print("Binding created successfully!")
            }
        }
    }
}
