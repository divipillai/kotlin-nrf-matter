//
//  LocalMatterDoorController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 25/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import OSLog

/// Namespace for identifiers of the door lock cluster.
enum DoorLockCluster {
    static let id: NSNumber = 0x0101
    enum Attribute {
        static let lockState: NSNumber = 0x0000
    }
}

/// Controls a door-lock type Matter device in the local fabric.
class LocalMatterDoorController : MatterDoorLockController {

    /// Locks or unlocks the door.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - isLocked: `true` to lock the door, `false` to unlock it.
    ///   - endpoint: The endpoint hosting the Door Lock cluster.
    /// - Throws: An error if the local controller cannot be obtained or the command fails.
    func lockUnlockDoor(deviceId: DeviceId, isLocked: Bool, endpoint: Int32) async throws {
        SharedLogger.debug(#function)
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)

        let cluster = MTRBaseClusterDoorLock(device: baseDevice, endpointID: endpoint as NSNumber, queue: DispatchQueue.global())
        if (isLocked) {
            try await cluster?.lockDoor()
        } else {
            try await cluster?.unlockDoor()
        }
    }
    
    /// Subscribes to door lock state changes reported by the device.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - endpoint: The endpoint hosting the Door Lock cluster.
    /// - Returns: A flow emitting the current lock state.
    /// - Throws: An error if the local controller cannot be obtained.
    func observeLockState(deviceId: DeviceId, endpoint: Int32) async throws -> any Kotlinx_coroutines_coreFlow {
        SharedLogger.debug(#function)
        let flowWrapper = IosFlowWrapper<LockDeviceState>()
        let observer = try AttributeSubscriber.shared(deviceId: deviceId.nsNumber())

        observer.subscribe(endpoint: endpoint as NSNumber, cluster: DoorLockCluster.id, attribute: DoorLockCluster.Attribute.lockState) { (result: Int32) in
            SharedLogger.debug("Received door lock state: \(result)")
            flowWrapper.emit(value: LockDeviceState.companion.create(value: result))
        }

        return flowWrapper.flow
    }
}
