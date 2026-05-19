//
//  LocalMatterBinder.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 25/03/2026.
//

import ComposeApp
import Matter
import SharedCode

class LocalMatterBinder : MatterBinder {

    func bind(sourceNodeId: DeviceId, sourceEndpoint: Int32, targetNodeId: DeviceId, targetEndpoint: Int32, clusterId: Int64) async throws {
        SharedLogger.debug("bindSwitchToLight")
        SharedLogger.info("Source node id: \(sourceNodeId)")
        SharedLogger.info("Target node it: \(targetNodeId)")
        
        let source = sourceNodeId.nsNumber()
        let target = targetNodeId.nsNumber()
        let sourceEnd = sourceEndpoint as NSNumber
        let targetEnd = targetEndpoint as NSNumber
        let cluster = clusterId as NSNumber
        
        let controller = try LocalControllerProvider(logTag: "LocalMatterBinder").getController()
        SharedLogger.info("Granting access to source.")
        await grantAccessToSource(targetDeviceID: target, sourceNodeID: source, clusterID: cluster, controller: controller)
        SharedLogger.info("Preparing binding.")
        await bindSwitchToBulb(sourceDeviceID: source, sourceEndpoint: sourceEnd, targetNodeID: target, targetEndpoint: targetEnd, clusterID: cluster, controller: controller)
    }

    func grantAccessToSource(targetDeviceID: NSNumber, sourceNodeID: NSNumber, clusterID: NSNumber, controller: MTRDeviceController) async {
        let targetDevice = MTRBaseDevice(nodeID: targetDeviceID, controller: controller)
        guard let aclCluster = MTRBaseClusterAccessControl(device: targetDevice, endpointID: 0, queue: .main) else { return }
        
        let target = MTRAccessControlClusterAccessControlTargetStruct()
        target.cluster = clusterID // On/Off cluster
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
                SharedLogger.info("Access granted successfully to node \(sourceNodeID)")
            } else {
                SharedLogger.info("ACL entry already exists for node \(sourceNodeID). Skipping write.")
            }
            
        } catch {
            SharedLogger.error("ACL read/write failed: \(error.localizedDescription)")
        }
    }
    
    func bindSwitchToBulb(sourceDeviceID: NSNumber, sourceEndpoint: NSNumber, targetNodeID: NSNumber, targetEndpoint: NSNumber, clusterID: NSNumber, controller: MTRDeviceController) async {
        let sourceDevice = MTRBaseDevice(nodeID: sourceDeviceID, controller: controller)
        guard let bindingCluster = MTRBaseClusterBinding(device: sourceDevice, endpointID: sourceEndpoint, queue: .main) else { return }
        
        let bindingEntry = MTRBindingClusterTargetStruct()
        bindingEntry.node = targetNodeID
        bindingEntry.endpoint = targetEndpoint
        bindingEntry.cluster = clusterID
        
        do {
            var bindings = try await bindingCluster.readAttributeBinding(with: nil)
            bindings.append(bindingEntry)
            try await bindingCluster.writeAttributeBinding(withValue: bindings)
            
            SharedLogger.info("Binding created successfully!")
        } catch {
            SharedLogger.error("Binding failed: \(error.localizedDescription)")
        }
    }
}
