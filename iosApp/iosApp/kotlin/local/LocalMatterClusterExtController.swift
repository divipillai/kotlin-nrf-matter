//
//  LocalMatterClusterExtController.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 30/04/2026.
//

import Matter
import ComposeApp
import SharedCode
import OSLog

class LocalMatterClusterExtController : MatterClusterExtensionController {

    private let endpointId: NSNumber = 0 //todo: hardcoded
    private let clusterId: NSNumber = 0x28 //todo: hardcoded
    private let attributeId: NSNumber = 0x17 //todo: hardcoded
    private let commandId: NSNumber = 0x00 //todo: hardcoded
    
    func getRandomNumber(deviceId: DeviceId) async throws -> KotlinInt {
        let attributeReader = try AttributeReader(deviceId: deviceId.nsNumber())
        let result: Int32 = try await attributeReader.readAttribute(endpoint: endpointId, cluster: clusterId, attribute: attributeId)
        return KotlinInt(int: result)
    }

    func generateRandomNumber(deviceId: DeviceId) async throws -> KotlinInt {
        SharedLogger.debug("Generating random number...")
        let commandExecutor = try CommandExecutor(deviceId: deviceId.nsNumber())
        
        try await commandExecutor.executeCommand(
            endpoint: endpointId,
            cluster: clusterId,
            command: commandId,
            type: MTRBooleanValueType,
            value: true
        )
        
        SharedLogger.debug("Generating random number command succeeded.")
        
        return try await getRandomNumber(deviceId: deviceId)
    }
    
    func subscribeToRandomNumber(deviceId: DeviceId, onUpdate: @escaping (KotlinInt) -> Void) async throws {
        SharedLogger.debug("Subscribe to random number.")
        let attributeSubscriber = try AttributeSubscriber(deviceId: deviceId.nsNumber())
        
        attributeSubscriber.subscribe(endpoint: endpointId, cluster: clusterId, attribute: attributeId, onUpdate: { result in
            onUpdate(KotlinInt(int: result))
            SharedLogger.debug("On new value: \(result)")
        })
    }
}
