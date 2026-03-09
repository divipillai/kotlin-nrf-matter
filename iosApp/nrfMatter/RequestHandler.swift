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
    
    private let commissioner = MatterCommissioner()
    
    enum PairingError: Error {
        case invalidCredentials
        case pairingFailed
    }

    private let logger = Logger(subsystem: "nrf.matter", category: "RequestHandler")

    override init() {
        super.init()
        logger.debug("MatterAddDeviceExtensionRequestHandler initialized")
        
        let sharedStorage = MatterStorage()
        sharedStorage.getKey(forKey: "Hello")
    }

    override func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        logger.debug("Received request to fetch rooms in home: \(String(describing: home?.displayName)).")

        let rooms: [String] = ["Living Room", "Bedroom", "Office", "Kitchen", "Dining Room", "AAATESTAAA"]
        return rooms.map { MatterAddDeviceRequest.Room(displayName: $0) }
    }

    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        logger.debug("Commissioning device in home '\(String(describing: home?.displayName))' with payload: \(onboardingPayload).")

        try await commissioner.commision(payload: onboardingPayload)
    }

    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        logger.debug("Configuring device '\(name)' in room: \(String(describing: room?.displayName))")
        
        commissioner.release()
    }

    override func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
        logger.debug("Validating device credential")
    }

    override func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {
        logger.debug("Selecting WiFi network from \(wifiScanResults.count) scan results")

        return .defaultSystemNetwork
    }
    
    override func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {
        logger.debug("Selecting Thread network from \(threadScanResults.count) scan results")

        let scanResult = threadScanResults[0] // .defaultSystemNetwork doesn't work. Selecting first.
        return ThreadNetworkAssociation.network(extendedPANID: scanResult.extendedPANID)
    }
}
