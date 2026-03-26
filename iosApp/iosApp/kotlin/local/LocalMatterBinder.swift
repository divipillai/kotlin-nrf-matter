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
        
        let newEntry = MTRAccessControlClusterAccessControlEntryStruct()
        newEntry.privilege = NSNumber(value: 3) // Operate
        newEntry.authMode = NSNumber(value: 2)  // CASE (Certificate-based)
        newEntry.subjects = [sourceNodeID]
        newEntry.targets = [target]
        
        do {
            var currentACLs = try await aclCluster.readAttributeACL(with: nil) as? [MTRAccessControlClusterAccessControlEntryStruct] ?? []
            let entryExists = currentACLs.contains { entry in
                (entry.subjects as? [NSNumber])?.contains(sourceNodeID) == true && entry.privilege == newEntry.privilege
            }
            
            if !entryExists {
                currentACLs.append(newEntry)
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
        
        let bindingEntry = MTRBindingClusterTargetStruct()
        bindingEntry.node = targetNodeID
        bindingEntry.endpoint = targetEndpoint
        bindingEntry.cluster = clusterID
        bindingEntry.fabricIndex = 1
        
        do {
            var bindings = try await bindingCluster.readAttributeBinding(with: nil)
            bindings.append(bindingEntry)
            try await bindingCluster.writeAttributeBinding(withValue: bindings)
            
            logger.info("Binding created successfully!")
        } catch {
            logger.error("Binding failed: \(error.localizedDescription)")
        }
    }
}
