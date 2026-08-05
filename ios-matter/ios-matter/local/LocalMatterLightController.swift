//
//  LocalMatterLightController.swift
//  ios-matter
//
//  Created by Sylwester Zielinski on 06/03/2026.
//

import Matter
import OSLog

/// Namespace for identifiers of the dimmable light device type.
enum DimmableLightDeviceType {
    
    enum OnOffCluster {
        static let id: NSNumber = 0x0006
        enum Attribute {
            static let onOff: NSNumber = 0x0000
        }
    }

    enum LevelControlCluster {
        static let id: NSNumber = 0x0008
        enum Attribute {
            static let currentLevel: NSNumber = 0x0000
        }
    }
}

/// Controls a light type Matter device in the local fabric.
@objc public final class LocalMatterLightController : NSObject {
    
    /// Turns the light on or off.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - isOn: `true` to turn the light on, `false` to turn it off.
    ///   - endpoint: The endpoint hosting the On/Off cluster.
    /// - Throws: An error if the local controller cannot be obtained or the command fails.
    @objc public func setDeviceOnOff(deviceId: NSNumber, isOn: Bool, endpoint: NSNumber) async throws {
        SwiftLogger.debug("Set device on/off = \(isOn)")
        SwiftLogger.debug("Endpoint: \(endpoint)")
        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: deviceId, controller: controller)

        let cluster = MTRBaseClusterOnOff(device: baseDevice, endpointID: endpoint as NSNumber, queue: DispatchQueue.global())
        SwiftLogger.debug("Cluster created: \(String(describing: cluster))")
        if (isOn) {
            try await cluster?.on()
        } else {
            try await cluster?.off()
        }
    }
    
    /// Sets the brightness level via the Level Control cluster.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - brightnessLevel: The target level, as defined by the Level Control cluster.
    ///   - endpoint: The endpoint hosting the Level Control cluster.
    /// - Throws: An error if the local controller cannot be obtained or the command fails.
    @objc public func setBrightnessLevel(deviceId: NSNumber, brightnessLevel: NSNumber, endpoint: NSNumber) async throws {
        SwiftLogger.debug("Set brightess level: \(brightnessLevel)")

        let controller = try LocalControllerProvider(logTag: "LocalControllerProvider").getController()
        let baseDevice = MTRBaseDevice(nodeID: deviceId, controller: controller)

        let cluster = MTRBaseClusterLevelControl(
            device: baseDevice,
            endpointID: endpoint,
            queue: DispatchQueue.global()
        )

        SwiftLogger.debug("Cluster created: \(String(describing: cluster))")

        let params = MTRLevelControlClusterMoveToLevelParams()
        params.level = brightnessLevel
        try await cluster?.moveToLevel(with: params)
    }
    
    /// Subscribes to on/off state changes reported by the device.
    ///
    /// The subscription is not cancellable and lives as long as the shared ``AttributeSubscriber``
    /// for this node.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - endpoint: The endpoint hosting the On/Off cluster.
    ///   - onUpdate: Called on a background queue with `true` when the light is on, `false` when it
    ///     is off — including once with the initial state when the subscription is established.
    /// - Throws: An error if the local controller cannot be obtained.
    @objc public func observeLightState(deviceId: NSNumber, endpoint: NSNumber, onUpdate: @escaping (Bool) -> Void) async throws {
        SwiftLogger.debug("subscribeToLedChanges")
        let observer = try AttributeSubscriber.shared(deviceId: deviceId)

        observer.subscribe(endpoint: endpoint as NSNumber, cluster: DimmableLightDeviceType.OnOffCluster.id, attribute: DimmableLightDeviceType.OnOffCluster.Attribute.onOff) { (result: Bool) in
            SwiftLogger.info("Received led state: \(result)")
            onUpdate(result)
        }
    }
    
    /// Subscribes to brightness level changes reported by the device.
    ///
    /// Raw level values from the Level Control cluster are normalized to a `0...1` range
    /// before being delivered, mapping the cluster's `1...254` range and clamping anything outside
    /// it.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - endpoint: The endpoint hosting the Level Control cluster.
    ///   - onUpdate: Called on a background queue with the brightness normalized to `0...1`.
    /// - Throws: An error if the local controller cannot be obtained.
    @objc public func observeBrightnessState(deviceId: NSNumber, endpoint: NSNumber, onUpdate: @escaping (Float) -> Void) async throws {
        SwiftLogger.debug("subscribeToLightLevelChanges")
        let observer = try AttributeSubscriber.shared(deviceId: deviceId)

        observer.subscribe(endpoint: endpoint as NSNumber, cluster: DimmableLightDeviceType.LevelControlCluster.id, attribute: DimmableLightDeviceType.LevelControlCluster.Attribute.currentLevel) { (rawLevel: Int) in
            SwiftLogger.info("Received light level: \(rawLevel)")
            let percent = max(0, min(1, (Float(rawLevel) - 1) / 253))
            SwiftLogger.info("Calculated percent: \(percent)")
            onUpdate(percent)
        }
    }
}
