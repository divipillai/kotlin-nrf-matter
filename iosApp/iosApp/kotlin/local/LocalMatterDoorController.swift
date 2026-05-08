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
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)

        let cluster = MTRBaseClusterDoorLock(device: baseDevice, endpointID: endpoint as NSNumber, queue: DispatchQueue.global())
        SharedLogger.debug("Cluster created: \(cluster)")
        if (isLocked) {
            try await cluster?.lockDoor()
        } else {
            try await cluster?.unlockDoor()
        }
    }
}
