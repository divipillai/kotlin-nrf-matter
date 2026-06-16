//
//  LocalMatterLightController.swift
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
class LocalMatterLightController : MatterLightController {

    /**
     * Set the light on/off on a remote Matter device.
     */
    func setDeviceOnOff(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        SharedLogger.debug("Set device on/off = \(isOn)")
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
     * Set the brightness level on a remote Matter device.
     */
    func setBrightnessLevel(deviceId: DeviceId, level: Int32, endpoint: Int32) async throws {
        SharedLogger.debug("Set brightess level: \(level)")

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
    
    /**
     * Observe if a device is on changes on a remote Matter device.
     */
    func subscribeToLedChanges(deviceId: DeviceId, endpoint: Int32, onUpdate: @escaping (KotlinBoolean) -> Void) async throws {
        SharedLogger.debug("subscribeToLedChanges")
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)

        let cluster = MTRBaseClusterOnOff(device: baseDevice, endpointID: endpoint as NSNumber, queue: DispatchQueue.global())
        SharedLogger.info("Cluster: \(cluster)")
        cluster?.subscribeAttributeOnOff(with: MTRSubscribeParams.defaultParams, subscriptionEstablished: { }, reportHandler: { result, error in
            if let result {
                SharedLogger.info("Received led state: \(result)")
                onUpdate(KotlinBoolean(bool: result.boolValue))
            }
            if let error {
                SharedLogger.debug("Received led on error: \(error)")
            }
        })
    }
    
    /**
     * Observe light level changes on a remote Matter device.
     */
    func subscribeToLightLevelChanges(deviceId: DeviceId, endpoint: Int32, onUpdate: @escaping (KotlinFloat) -> Void) async throws {
        SharedLogger.debug("subscribeToLightLevelChanges")
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: deviceId.nsNumber(), controller: controller)

        let cluster = MTRBaseClusterLevelControl(device: baseDevice, endpointID: endpoint as NSNumber, queue: DispatchQueue.global())
        SharedLogger.info("Cluster: \(cluster)")
        cluster?.subscribeAttributeCurrentLevel(with: MTRSubscribeParams.defaultParams, subscriptionEstablished: { }, reportHandler: { result, error in
            if let result {
                SharedLogger.info("Received light level: \(result)")
                let rawLevel = result.intValue
                let percent = max(0, min(1, (Float(rawLevel) - 1) / 253))
                SharedLogger.info("Calculated percent: \(percent)")
                onUpdate(KotlinFloat(float: percent))
            }
            if let error {
                SharedLogger.debug("Received light level error: \(error)")
            }
        })
    }
}
