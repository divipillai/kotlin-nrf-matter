//
//  LocalMatterBinder.swift
//  ios-matter
//
//  Created by Sylwester Zielinski on 25/03/2026.
//

import Matter

/// Binds Matter device clusters directly to each other for device-to-device
/// control, bypassing the controller.
@objc public class LocalMatterBinder : NSObject {

    /// Binds a source device and a target device together.
    ///
    /// The source device will send commands directly to the target without going through a
    /// controller. The set of supported commands is defined by the cluster, which is declared
    /// as a client cluster on the source and as a server cluster on the target.
    ///
    /// - Parameters:
    ///   - source: The node ID of the device that will send commands.
    ///   - sourceEndpoint: The endpoint on the source device that hosts the client cluster.
    ///   - target: The node ID of the device that will receive commands.
    ///   - targetEndpoint: The endpoint on the target device that hosts the server cluster.
    ///   - cluster: The cluster ID used for the binding.
    /// - Throws: An error if the local controller cannot be obtained, or if granting access on the
    ///   target or creating the binding on the source fails.
    @objc public func bind(source: NSNumber, sourceEndpoint: NSNumber, target: NSNumber, targetEndpoint: NSNumber, cluster: NSNumber) async throws {
        SwiftLogger.info("Binding clusters...")
        SwiftLogger.debug("Source node id: \(source)")
        SwiftLogger.debug("Target node it: \(target)")
        
        let controller = try LocalControllerProvider(logTag: "LocalMatterBinder").getController()
        SwiftLogger.info("Granting access to source.")
        try await grantAccessToSource(targetDeviceID: target, sourceNodeID: source, clusterID: cluster, controller: controller)
        SwiftLogger.info("Preparing binding.")
        try await bindSwitchToBulb(sourceDeviceID: source, sourceEndpoint: sourceEndpoint, targetNodeID: target, targetEndpoint: targetEndpoint, clusterID: cluster, controller: controller)
        SwiftLogger.info("Binding successful.")
    }

    /// Grants a source device access to a target device's cluster via an ACL entry.
    ///
    /// Existing ACL entries are preserved so the commissioner can continue to control the
    /// target; the new entry is only appended if an equivalent one does not already exist.
    ///
    /// Privilege levels:
    /// - View: for reading attributes.
    /// - Operate: for sending commands.
    /// - Manage: for bindings and subscriptions, without changing privileges, ACL settings, etc.
    /// - Administer: all permissions, available to the commissioner.
    ///
    /// Auth mode:
    /// - CASE: for secure one-to-one communication.
    /// - Group: less secure but suitable for one-to-many communication.
    ///
    /// - Parameters:
    ///   - targetDeviceID: The node ID of the device whose ACL is being modified.
    ///   - sourceNodeID: The node ID being granted access.
    ///   - clusterID: The cluster ID the access grant applies to.
    ///   - controller: The Matter controller used to reach the target device.
    private func grantAccessToSource(targetDeviceID: NSNumber, sourceNodeID: NSNumber, clusterID: NSNumber, controller: MTRDeviceController) async throws {
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
        
        SwiftLogger.info("Reading attribute ACL...")
        var currentACLs = try await aclCluster.readAttributeACL(with: nil) as? [MTRAccessControlClusterAccessControlEntryStruct] ?? []
        SwiftLogger.info("Amending ACL records...")
        let entryExists = currentACLs.contains { entry in
            (entry.subjects as? [NSNumber])?.contains(sourceNodeID) == true && entry.privilege == newEntry.privilege
        }
        
        if !entryExists {
            SwiftLogger.info("Storing new ACL record on the target device...")
            currentACLs.append(newEntry)
            try await aclCluster.writeAttributeACL(withValue: currentACLs)
            SwiftLogger.debug("Access granted successfully to node \(sourceNodeID)")
        } else {
            SwiftLogger.debug("ACL entry already exists for node \(sourceNodeID). Skipping write.")
        }
    }
    
    /// Creates a binding so the source device can send commands directly to the target device.
    ///
    /// The set of supported commands is defined by the cluster. The source must support those
    /// commands and should declare that capability in its descriptor cluster for the endpoint as a
    /// client cluster. The target must implement the cluster and should declare it in its own
    /// descriptor cluster for the endpoint as a server cluster.
    ///
    /// The new binding entry is appended to the source's existing binding list so previously
    /// defined bindings are preserved.
    ///
    /// - Parameters:
    ///   - sourceDeviceID: The node ID of the device that will send commands.
    ///   - sourceEndpoint: The endpoint on the source device that hosts the client cluster.
    ///   - targetNodeID: The node ID of the device that will receive commands.
    ///   - targetEndpoint: The endpoint on the target device that hosts the server cluster.
    ///   - clusterID: The cluster ID used for the binding.
    ///   - controller: The Matter controller used to reach the source device.
    private func bindSwitchToBulb(sourceDeviceID: NSNumber, sourceEndpoint: NSNumber, targetNodeID: NSNumber, targetEndpoint: NSNumber, clusterID: NSNumber, controller: MTRDeviceController) async throws {
        let sourceDevice = MTRBaseDevice(nodeID: sourceDeviceID, controller: controller)
        guard let bindingCluster = MTRBaseClusterBinding(device: sourceDevice, endpointID: sourceEndpoint, queue: .main) else { return }
        
        SwiftLogger.debug("Preparing a new binding record.")
        let bindingEntry = MTRBindingClusterTargetStruct()
        bindingEntry.node = targetNodeID
        bindingEntry.endpoint = targetEndpoint
        bindingEntry.cluster = clusterID
        
        SwiftLogger.debug("Storing record on a source device.")
        var bindings = try await bindingCluster.readAttributeBinding(with: nil)
        bindings.append(bindingEntry)
        try await bindingCluster.writeAttributeBinding(withValue: bindings)
        
        SwiftLogger.debug("Binding created successfully on source.")
    }
}
