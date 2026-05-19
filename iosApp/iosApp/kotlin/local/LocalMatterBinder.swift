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

    /**
     * Binds a source and a target Matter devices.
     * The source will be sending commands directly to the target without using of a controller.
     * The set of supported commands is defined in the cluster.
     * The cluster is defined as a client cluster for the source and as a server cluster for the target.
     */
    func bind(sourceNodeId: DeviceId, sourceEndpoint: Int32, targetNodeId: DeviceId, targetEndpoint: Int32, clusterId: Int64) async throws {
        SharedLogger.info("Binding clusters...")
        SharedLogger.debug("Source node id: \(sourceNodeId)")
        SharedLogger.debug("Target node it: \(targetNodeId)")
        
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
        SharedLogger.info("Binding successful.")
    }

    /**
     * Open access on a target so it can be controlled by another Matter device.
     * It is important not to delete already existing records so the commissioner can
     * still control the target.
     *
     * Privilate levels:
     * View - for reading attributes.
     * Operate - for sending commands.
     * Manage - for bindings and subscriptions but without changing privilages, settings ACL etc.
     * Administer - all permissions, available for commissioner.
     *
     * Auth mode:
     * CASE - for secure 1 to 1 communication.
     * Group - less secure but suitable for 1 to N communication.
     */
    func grantAccessToSource(targetDeviceID: NSNumber, sourceNodeID: NSNumber, clusterID: NSNumber, controller: MTRDeviceController) async {
        let targetDevice = MTRBaseDevice(nodeID: targetDeviceID, controller: controller)
        guard let aclCluster = MTRBaseClusterAccessControl(device: targetDevice, endpointID: 0, queue: .main) else { return }
        
        let target = MTRAccessControlClusterAccessControlTargetStruct()
        target.cluster = clusterID
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
                SharedLogger.debug("Access granted successfully to node \(sourceNodeID)")
            } else {
                SharedLogger.debug("ACL entry already exists for node \(sourceNodeID). Skipping write.")
            }
            
        } catch {
            SharedLogger.error("ACL read/write failed: \(error.localizedDescription)")
        }
    }
    
    /**
     * Tells the source to send commands directly to another Matter device.
     *
     * The set of supported commands is defined by the cluster.
     * The source should be able to support those commands and be able to send them to the target.
     * The source declares that capability in descriptor cluster for the endpoint as a client cluster.
     *
     * The target needs to implement this cluster and is responsible for accepting commands.
     * The target should declare that it is using this cluster in cluster descriptor for the endpoint as
     * a server cluster.
     *
     * The binding record should be appended to not delete previously defined bindings. 
     */
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
            
            SharedLogger.debug("Binding created successfully on source.")
        } catch {
            SharedLogger.error("Binding failed: \(error.localizedDescription)")
        }
    }
}
