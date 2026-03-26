//
//  RequestHandler.swift
//  nrfMatter
//
//  Created by Sylwester Zielinski on 24/02/2026.
//
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
    
    private let handler: RequestHandlerProtocol = {
        let storage = SharedStorage(suitName: SharedConsts.sharedStorage)
        let value = storage.getString(key: SharedConsts.matterEnvStorageKey)
        let env = MatterEnv(rawValue: value!)
        
        return switch env {
        case .local:
            LocalRequestHandler()
        case .google:
            GoogleRequestHandler()
        default:
            fatalError("Invalid environment")
        }
    }()
    
    enum HandlerError: Error {
        case invalidEnvironment
    }

    private let logger = Logger(subsystem: "nrf.matter", category: "RequestHandler")

    override func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        logger.info("Received request to fetch rooms in home: \(String(describing: home?.displayName)).")

        return await handler.rooms(in: home)
    }

    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        logger.info("Commissioning device in home '\(String(describing: home?.displayName))' with payload: \(onboardingPayload).")

        try await handler.commissionDevice(in: home, onboardingPayload: onboardingPayload, commissioningID: commissioningID)
    }

    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        logger.info("Configuring device '\(name)' in room: \(String(describing: room?.displayName))")
        
        await handler.configureDevice(named: name, in: room)
    }

    override func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
        logger.info("Validating device credential")
        
        try await handler.validateDeviceCredential(deviceCredential)
    }

    override func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {
        logger.info("Selecting WiFi network from \(wifiScanResults.count) scan results")

        return try await handler.selectWiFiNetwork(from: wifiScanResults)
    }
    
    override func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {
        logger.info("Selecting Thread network from \(threadScanResults.count) scan results")
        
        threadScanResults.forEach { item in
            logger.info("AAATESTAAA - thread network: \(item.networkName, privacy: .public)")
        }

        return try await handler.selectThreadNetwork(from: threadScanResults)
    }
}
