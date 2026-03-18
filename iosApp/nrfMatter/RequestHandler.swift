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
    
    private let handler: MatterAddDeviceExtensionRequestHandler
    private let commissioner = MatterCommissioner()
    
    enum HandlerError: Error {
        case invalidEnvironment
    }

    private let logger = Logger(subsystem: "nrf.matter", category: "RequestHandler")

    override init() {
        logger.info("AAATESTAAAA - init")
        let storage = MatterStorage()
        let value = storage.getString(key: SharedConsts.matterEnvStorageKey)
        let env = MatterEnv(rawValue: value!)
        
        switch env {
        case .local:
            handler = LocalRequestHandler()
        case .google:
            handler = GoogleRequestHandler()
        default:
            fatalError("Invalid environment")
        }
        
        super.init()
        
        logger.info("AAATESTAAAA - env: \(env?.rawValue ?? "unknown")")
        
        logger.info("AAATESTAAAA -MatterAddDeviceExtensionRequestHandler initialized")
    }

    override func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        logger.info("AAATESTAAAA -Received request to fetch rooms in home: \(String(describing: home?.displayName)).")

        return await handler.rooms(in: home)
    }

    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        logger.info("AAATESTAAAA -Commissioning device in home '\(String(describing: home?.displayName))' with payload: \(onboardingPayload).")

        try await handler.commissionDevice(in: home, onboardingPayload: onboardingPayload, commissioningID: commissioningID)
    }

    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        logger.info("AAATESTAAAA -Configuring device '\(name)' in room: \(String(describing: room?.displayName))")
        
        await handler.configureDevice(named: name, in: room)
    }

    override func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
        logger.info("AAATESTAAAA -Validating device credential")
        
        try await handler.validateDeviceCredential(deviceCredential)
    }

    override func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {
        logger.info("AAATESTAAAA -Selecting WiFi network from \(wifiScanResults.count) scan results")

        return try await handler.selectWiFiNetwork(from: wifiScanResults)
    }
    
    override func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {
        logger.info("AAATESTAAAA -Selecting Thread network from \(threadScanResults.count) scan results")

        return try await handler.selectThreadNetwork(from: threadScanResults)
    }
}
