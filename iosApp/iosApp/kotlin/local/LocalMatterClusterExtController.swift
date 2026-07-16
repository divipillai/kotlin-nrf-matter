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

/// Namespace for identifiers used by the custom Basic Information cluster extension.
enum BasicInformationClusterExtension {

    static let id: NSNumber = 0x28 // Basic Information Cluster
    
    enum Attribute {
        static let randomNumber: NSNumber = 0x17 // New field
    }
    
    enum Command {
        static let generateRandomNumber: NSNumber = 0x00 // New command
    }
}

/// Communicates with a custom extension to the standard Basic Information cluster that adds a
/// random number field and a command to generate it.
///
/// The flow is to first send the "generate number" command, then read the new value from the
/// attribute.
class LocalMatterClusterExtController : MatterClusterExtensionController {

    /// Sends the "generate random number" command and reads back the newly generated value.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - endpoint: The endpoint hosting the cluster extension.
    /// - Returns: The newly generated random number.
    /// - Throws: An error if the command invocation or the subsequent attribute read fails.
    func generateRandomNumber(deviceId: DeviceId, endpoint: Int32) async throws -> KotlinLong {
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
    
    /// Reads the current value of the "random number" attribute.
    ///
    /// - Parameters:
    ///   - deviceId: The Matter node ID of the target device.
    ///   - endpointId: The endpoint hosting the cluster extension.
    /// - Returns: The current random number value.
    /// - Throws: An error if the attribute read fails.
    private func getRandomNumber(deviceId: DeviceId, endpointId: NSNumber) async throws -> KotlinLong {
        let attributeReader = try AttributeReader(deviceId: deviceId.nsNumber())
        let result: Int32 = try await attributeReader.readAttribute(
            endpoint: endpointId,
            cluster: BasicInformationClusterExtension.id,
            attribute: BasicInformationClusterExtension.Attribute.randomNumber
        )
        return KotlinLong(longLong: Int64(result))
    }
}
