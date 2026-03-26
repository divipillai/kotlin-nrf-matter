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
        let switchId = 33 as NSNumber // todo
        let lightId = 34 as NSNumber // todo
        let controller = try LocalControllerProvider(logTag: "LocalMatterBinder").getController()!
        logger.info("acl")
        await grantAccessToSource(targetDeviceID: lightId, sourceNodeID: switchId, controller: controller)
        logger.info("bind")
        await bindSwitchToBulb(sourceDeviceID: switchId, sourceEndpoint: 1, targetNodeID: lightId, targetEndpoint: 1, clusterID: 6, controller: controller)
    }

    func grantAccessToSource(targetDeviceID: NSNumber, sourceNodeID: NSNumber, controller: MTRDeviceController) async {
        let targetDevice = MTRBaseDevice(nodeID: targetDeviceID, controller: controller)
        guard let aclCluster = MTRBaseClusterAccessControl(device: targetDevice, endpointID: 0, queue: .main) else { return }
        
        let target = MTRAccessControlClusterAccessControlTargetStruct()
        target.cluster = NSNumber(value: 6) // On/Off cluster
        target.endpoint = nil
        target.deviceType = nil
        
        // Create an entry allowing the source node to 'Operate' this device
        let newEntry = MTRAccessControlClusterAccessControlEntryStruct()
        newEntry.privilege = NSNumber(value: 3) // Operate
        newEntry.authMode = NSNumber(value: 2)  // CASE (Certificate-based)
        newEntry.subjects = [sourceNodeID]
        newEntry.targets = [target] // nil means all clusters/endpoints on this node
        // newEntry.fabricIndex = nil // Let the device infer this based on the accessing fabric
        
        do {
            // 1. Read existing ACLs to prevent locking out the Admin controller
            var currentACLs = try await aclCluster.readAttributeACL(with: nil) as? [MTRAccessControlClusterAccessControlEntryStruct] ?? []
            
            // Optional: Check if the entry already exists to avoid duplicates
            let entryExists = currentACLs.contains { entry in
                (entry.subjects as? [NSNumber])?.contains(sourceNodeID) == true && entry.privilege == newEntry.privilege
            }
            
            if !entryExists {
                // 2. Append the new entry
                currentACLs.append(newEntry)
                
                // 3. Write the combined ACL back
                try await aclCluster.writeAttributeACL(withValue: currentACLs)
                logger.info("Access granted successfully to node \(sourceNodeID)")
            } else {
                logger.info("ACL entry already exists for node \(sourceNodeID). Skipping write.")
            }
            
        } catch {
            logger.error("ACL read/write failed: \(error.localizedDescription)")
        }
    }
    
    func bindSwitchToBulb(sourceDeviceID: NSNumber, sourceEndpoint: NSNumber, targetNodeID: NSNumber, targetEndpoint: NSNumber, clusterID: NSNumber, controller: MTRDeviceController) async {
        let sourceDevice = MTRBaseDevice(nodeID: sourceDeviceID, controller: controller)
        guard let bindingCluster = MTRBaseClusterBinding(device: sourceDevice, endpointID: sourceEndpoint, queue: .main) else { return }
        
        logger.info("222")
        
        let bindingEntry = MTRBindingClusterTargetStruct()
        bindingEntry.node = targetNodeID
        bindingEntry.endpoint = targetEndpoint
        bindingEntry.cluster = clusterID // e.g., 6 for OnOff, 8 for LevelControl
        bindingEntry.fabricIndex = 1 // Fix: Do not force fabric index on write
        
        do {
            logger.info("333")
            // Note: Depending on your use case, you may also want to read-modify-write bindings here
            // so you don't overwrite existing switches bound to this device.
            var bindings = try await bindingCluster.readAttributeBinding(with: nil)

            bindings.append(bindingEntry)
            
            logger.info("444")
            
            try await bindingCluster.writeAttributeBinding(withValue: bindings)
            logger.info("Binding created successfully!")
        } catch {
            logger.error("Binding failed: \(error.localizedDescription)")
        }
    }
}
