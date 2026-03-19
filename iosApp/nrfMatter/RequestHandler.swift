//
//  RequestHandler.swift
//  nrfMatter
//
//  Created by Sylwester Zielinski on 24/02/2026.
//

import GoogleHomeSDK
import MatterSupport
import Matter
import OSLog
import SharedCode

final class RequestHandler: MatterAddDeviceExtensionRequestHandler {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "RequestHandler")
    
    override func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        logger.info("AAATESTAAAA - rooms")
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
          let fetchedRooms = try homeMatterCommissioner.fetchRooms()
          // Returning fetched rooms.
          return fetchedRooms
        } catch {
          // Failed to fetch rooms with error
            logger.info("AAATESTAAAA - fatal")
            let rooms: [String] = ["Fallback"]
            return rooms.map { MatterAddDeviceRequest.Room(displayName: $0) }
        }
    }

    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        logger.info("AAATESTAAAA - commissionDevice")
        logger.info("AAATESTAAAA - payload: \(onboardingPayload, privacy: .public)")
        let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
        try await homeMatterCommissioner.commissionMatterDevice(onboardingPayload: onboardingPayload)
    }

    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        logger.info("AAATESTAAAA - configureDevice")
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
          try await homeMatterCommissioner.configureMatterDevice(deviceName: name, roomName: room?.displayName)
        } catch {
          // Configure Device failed with error
            logger.info("AAATESTAAAA - fatal")
        }
    }

    override func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
        logger.info("AAATESTAAAA - validateDeviceCredential")
    }

    override func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {
        logger.info("AAATESTAAAA - selectWiFiNetwork")
        return .defaultSystemNetwork
    }
    
    override func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {
        logger.info("AAATESTAAAA - selectThreadNetwork")
        let scanResult = threadScanResults[0] // .defaultSystemNetwork doesn't work. Selecting first.
        return ThreadNetworkAssociation.network(extendedPANID: scanResult.extendedPANID)
    }
}
