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

/**
 * A helper class from controlling a door type Matter device.
 */
class LocalMatterDoorController : MatterDoorController {

    /**
     * Lock/unlock the door.
     */
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
    
    /**
     * Observe door lock state changes on a remote Matter device.
     */
    func subscribeToLockChanges(deviceId: DeviceId, endpoint: Int32, onUpdate: @escaping (KotlinBoolean) -> Void) async throws {
        SharedLogger.debug(#function)
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)

        let cluster = MTRBaseClusterDoorLock(device: baseDevice, endpointID: endpoint as NSNumber, queue: DispatchQueue.global())
        
        cluster!.subscribeAttributeLockState(with: MTRSubscribeParams.defaultParams, subscriptionEstablished: { }, reportHandler: { result, error in
            if let result {
                SharedLogger.debug("Received door lock state: \(result)")
                onUpdate(KotlinBoolean(bool: result == 1 ? true : false)) // 1 = Locked, 2 = Unlocked
            }
            if let error {
                SharedLogger.debug("Received door lock error: \(error)")
            }
        })
    }
}
