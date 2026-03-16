//
//  RequestHandler.swift
//  nrfMatter
//
//  Created by Sylwester Zielinski on 24/02/2026.
//

import MatterSupport
import Matter
import OSLog
import SharedCode

final class RequestHandler: MatterAddDeviceExtensionRequestHandler {
    
    private let handler: MatterAddDeviceExtensionRequestHandler?
    private let commissioner = MatterCommissioner()
    
    enum PairingError: Error {
        case invalidCredentials
        case pairingFailed
    }

    private let logger = Logger(subsystem: "nrf.matter", category: "RequestHandler")

    override init() {
        let storage = MatterStorage()
        let value = storage.getString(key: "Technology")
        let env = MatterEnv(rawValue: value!)
        
        handler = switch env {
        case .local: LocalRequestHandler()
        case .google: GoogleRequestHandler()
        default: nil
        }
        
        super.init()
        
        logger.debug("MatterAddDeviceExtensionRequestHandler initialized")
    }

    override func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        logger.debug("Received request to fetch rooms in home: \(String(describing: home?.displayName)).")

        return await handler?.rooms(in: home) ?? []
    }

    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        logger.debug("Commissioning device in home '\(String(describing: home?.displayName))' with payload: \(onboardingPayload).")

        try await handler?.commissionDevice(in: home, onboardingPayload: onboardingPayload, commissioningID: commissioningID)
    }

    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        logger.debug("Configuring device '\(name)' in room: \(String(describing: room?.displayName))")
        
        await handler?.configureDevice(named: name, in: room)
    }

    override func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
        logger.debug("Validating device credential")
        
        try await validateDeviceCredential(deviceCredential)
    }

    override func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {
        logger.debug("Selecting WiFi network from \(wifiScanResults.count) scan results")

        return try await selectWiFiNetwork(from: wifiScanResults)
    }
    
    override func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {
        logger.debug("Selecting Thread network from \(threadScanResults.count) scan results")

        return try await selectThreadNetwork(from: threadScanResults)
    }
}
