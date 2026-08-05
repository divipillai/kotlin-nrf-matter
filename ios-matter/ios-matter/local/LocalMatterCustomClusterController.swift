//
//  LocalMatterCustomClusterController.swift
//  ios-matter
//
//  Created by Sylwester Zielinski on 20/04/2026.
//

import Matter
import OSLog

/// Namespace for identifiers of the manufacturer-specific cluster used in this example.
enum ManufacturerSpecificCluster {

    static let id: NSNumber = 0xFFF1FC01
    
    enum Attribute {
        static let name: NSNumber = 0xfff10000
        static let led: NSNumber = 0xfff10001
        static let button: NSNumber = 0xfff10002
    }
    
    enum Command {
        static let setLed: NSNumber = 0xFFF10000
    }
}

/// Values read from the manufacturer-specific cluster.
///
/// Declared as an `@objc` class rather than a struct so it can cross the Objective-C bridge
/// into Kotlin/Native as part of ``DeviceMatterInfo``.
@objc public final class ManufacturerSpecificData: NSObject {
    @objc public let name: String
    @objc public let led: Bool
    @objc public let button: Bool

    @objc public init(name: String, led: Bool, button: Bool) {
        self.name = name
        self.led = led
        self.button = button
        super.init()
    }
}

/// Communicates with the manufacturer-specific cluster defined by this example's firmware.
///
/// The cluster provides two features:
/// 1. Turning the LED on/off.
/// 2. Observing button state changes.
@objc public final class LocalMatterCustomClusterController : NSObject {

    /// Reads the custom attributes exposed by the manufacturer-specific cluster.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - endpoint: The endpoint hosting the cluster.
    /// - Returns: The device's custom name, LED state, and button state.
    /// - Throws: An error if any of the attribute reads fail.
    @objc public func getData(deviceId: NSNumber, endpoint: NSNumber) async throws -> ManufacturerSpecificData {
        SwiftLogger.debug("Getting custom manufacturer data...")
        
        let attributeReader = try AttributeReader(deviceId: deviceId)
       
        let name: String = try await attributeReader.readAttribute(endpoint: endpoint, cluster: ManufacturerSpecificCluster.id, attribute: ManufacturerSpecificCluster.Attribute.name)
        let led: Bool = try await attributeReader.readAttribute(endpoint: endpoint, cluster: ManufacturerSpecificCluster.id, attribute: ManufacturerSpecificCluster.Attribute.led)
        let button: Bool = try await attributeReader.readAttribute(endpoint: endpoint, cluster: ManufacturerSpecificCluster.id, attribute: ManufacturerSpecificCluster.Attribute.button)
        
        let data = ManufacturerSpecificData(
            name: name,
            led: led,
            button: button
        )
        
        return data
    }

    /// Sends a command to turn the LED on or off.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - isOn: `true` to turn the LED on, `false` to turn it off.
    ///   - endpoint: The endpoint hosting the cluster.
    /// - Throws: An error if the command invocation fails.
    @objc public func setLed(deviceId: NSNumber, isOn: Bool, endpoint: NSNumber) async throws {
        SwiftLogger.debug("invoke setLed")
        let commandExecutor = try CommandExecutor(deviceId: deviceId)
        
        try await commandExecutor.executeCommand(
            endpoint: endpoint,
            cluster: ManufacturerSpecificCluster.id,
            command: ManufacturerSpecificCluster.Command.setLed,
            type: MTRUnsignedIntegerValueType,
            value: isOn ? 1 : 0,
        )
    }
    
    /// Subscribes to button press state changes reported by the cluster.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - endpoint: The endpoint hosting the cluster.
    ///   - onUpdate: Called on a background queue with `true` when the button is pressed, `false`
    ///     when released.
    /// - Throws: An error if the local controller cannot be obtained.
    @objc public func observeButtonChanges(deviceId: NSNumber, endpoint: NSNumber, onUpdate: @escaping (Bool) -> Void) async throws {
        SwiftLogger.debug("Observe button changes")
        
        let attributeSubscriber = try AttributeSubscriber.shared(deviceId: deviceId)

        attributeSubscriber.subscribe(endpoint: endpoint, cluster: ManufacturerSpecificCluster.id, attribute: ManufacturerSpecificCluster.Attribute.button, onUpdate: { (result: Bool) in
            SwiftLogger.debug("Received new button state: \(result)")
            onUpdate(result)
        })
    }
}
