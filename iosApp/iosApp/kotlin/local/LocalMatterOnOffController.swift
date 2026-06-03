//
//  MatterOnOffController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 06/03/2026.
//

import ComposeApp
import Matter
import SharedCode
import OSLog

/**
 * A helper class from controlling a light type Matter device in a local fabric.
 */
class LocalMatterOnOffController : MatterOnOffController {

    /**
     * Set the light on/off on a remote Matter device.
     */
    func setDeviceOnOff(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)

        let cluster = MTRBaseClusterOnOff(device: baseDevice, endpointID: endpoint as NSNumber, queue: DispatchQueue.global())
        SharedLogger.debug("Cluster created: \(String(describing: cluster))")
        if (isOn) {
            try await cluster?.on()
        } else {
            try await cluster?.off()
        }
    }
    
    /**
     * Set the light level on a remote Matter device.
     */
    func setLevel(deviceId: DeviceId, isOn: Bool, endpoint: Int32, level: Int32) async throws {
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)

        let cluster = MTRBaseClusterLevelControl(
            device: baseDevice,
            endpointID: endpoint as NSNumber,
            queue: DispatchQueue.global()
        )

        SharedLogger.debug("Cluster created: \(String(describing: cluster))")

        let params = MTRLevelControlClusterMoveToLevelParams()
        params.level = NSNumber(value: level)
        try await cluster?.moveToLevel(with: params)
    }
}
