//
//  LocalMatterCustomClusterController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 20/04/2026.
//

import Matter
import ComposeApp
import SharedCode
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

/// Communicates with the manufacturer-specific cluster defined by this example's firmware.
///
/// The cluster provides two features:
/// 1. Turning the LED on/off.
/// 2. Observing button state changes.
class LocalMatterCustomClusterController: MatterManufacturerCustomDataController {

    /// Reads the custom attributes exposed by the manufacturer-specific cluster.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - endpoint: The endpoint hosting the cluster.
    /// - Returns: The device's custom name, LED state, and button state.
    /// - Throws: An error if any of the attribute reads fail.
    func getData(deviceId: DeviceId, endpoint: Int32) async throws -> ManufacturerSpecificData {
        SharedLogger.debug("Getting custom manufacturer data...")
        
        let attributeReader = try AttributeReader(deviceId: deviceId.nsNumber())
        let endpointId = NSNumber(value: endpoint)
       
        let name: String = try await attributeReader.readAttribute(endpoint: endpointId, cluster: ManufacturerSpecificCluster.id, attribute: ManufacturerSpecificCluster.Attribute.name)
        let led: Bool = try await attributeReader.readAttribute(endpoint: endpointId, cluster: ManufacturerSpecificCluster.id, attribute: ManufacturerSpecificCluster.Attribute.led)
        let button: Bool = try await attributeReader.readAttribute(endpoint: endpointId, cluster: ManufacturerSpecificCluster.id, attribute: ManufacturerSpecificCluster.Attribute.button)
        
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
    func setLed(deviceId: DeviceId, isOn: Bool, endpoint: Int32) async throws {
        SharedLogger.debug("invoke setLed")
        let commandExecutor = try CommandExecutor(deviceId: deviceId.nsNumber())
        let endpointId = NSNumber(value: endpoint)
        
        try await commandExecutor.executeCommand(
            endpoint: endpointId,
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
    ///   - onUpdate: Called with each new button state.
    func subscribeToButtonChanges(deviceId: DeviceId, endpoint: Int32, onUpdate: @escaping (KotlinBoolean) -> Void) async throws {
        let attributeSubscriber = try? AttributeSubscriber(deviceId: deviceId.nsNumber())
        let endpointId = NSNumber(value: endpoint)
        
        attributeSubscriber?.subscribe(endpoint: endpointId, cluster: ManufacturerSpecificCluster.id, attribute: ManufacturerSpecificCluster.Attribute.button, onUpdate: { result in
            onUpdate(KotlinBoolean(bool: result))
        })
    }
}
