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

enum BasicInformationClusterExtension {
    
    static let id: NSNumber = 0x28 // Basic Information Cluster
    
    enum Attribute {
        static let randomNumber: NSNumber = 0x17 // New field
    }
    
    enum Command {
        static let generateRandomNumber: NSNumber = 0x00 // New command
    }
}

/**
 * A helper class for communication with the cluster extension.
 * The extension defines additional field and command for generating a ranom number to Basic Information Cluster defined by standard.
 * The flow requires first to send a "generate number" command and latter for reading the new value from the attribute.
 */
class LocalMatterClusterExtController : MatterClusterExtensionController {
    
    /**
     * Sends a "generate random number" command which is defined as an extension to Basic Information Cluster defined by standard.
     */
    func generateRandomNumber(deviceId: DeviceId, endpoint: Int32) async throws -> KotlinInt {
        SharedLogger.debug("Generating random number...")
        let commandExecutor = try CommandExecutor(deviceId: deviceId.nsNumber())
        let endpointId = NSNumber(value: endpoint)
        
        try await commandExecutor.executeCommand(
            endpoint: endpointId,
            cluster: BasicInformationClusterExtension.id,
            command: BasicInformationClusterExtension.Command.generateRandomNumber,
            type: MTRBooleanValueType,
            value: true
        )
        
        SharedLogger.debug("Generating random number command succeeded.")
        
        return try await getRandomNumber(deviceId: deviceId, endpointId: endpointId)
    }
    
    /**
     * Reads value from a "random number" attibute.
     */
    private func getRandomNumber(deviceId: DeviceId, endpointId: NSNumber) async throws -> KotlinInt {
        let attributeReader = try AttributeReader(deviceId: deviceId.nsNumber())
        let result: Int32 = try await attributeReader.readAttribute(
            endpoint: endpointId,
            cluster: BasicInformationClusterExtension.id,
            attribute: BasicInformationClusterExtension.Attribute.randomNumber
        )
        return KotlinInt(int: result)
    }
}
