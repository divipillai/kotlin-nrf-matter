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
        logger.error("AAATESTAAAA - rooms")
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
          let fetchedRooms = try homeMatterCommissioner.fetchRooms()
          // Returning fetched rooms.
          return fetchedRooms
//            logger.error("AAATESTAAAA - fatal")
//            let rooms: [String] = ["Living Room", "Bedroom", "Office", "Kitchen", "Dining Room"]
//            return rooms.map { MatterAddDeviceRequest.Room(displayName: $0) }
        } catch {
          // Failed to fetch rooms with error
            logger.error("AAATESTAAAA - fatal")
            let rooms: [String] = ["Living Room", "Bedroom", "Office", "Kitchen", "Dining Room"]
            return rooms.map { MatterAddDeviceRequest.Room(displayName: $0) }
        }
    }

    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        logger.error("AAATESTAAAA - commissionDevice")
        logger.error("AAATESTAAAA - payload: \(onboardingPayload, privacy: .public)")
        let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
        do {
            try await homeMatterCommissioner.commissionMatterDevice(onboardingPayload: onboardingPayload)
        } catch {
            logger.error("AAATESTAAAA - fatal")
            logger.error("AAATESTAAAA - \(error)")
        }
    }

    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        logger.info("AAATESTAAAA - configureDevice")
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
          try await homeMatterCommissioner.configureMatterDevice(deviceName: name, roomName: room?.displayName)
        } catch {
          // Configure Device failed with error
            logger.error("AAATESTAAAA - fatal")
            logger.error("AAATESTAAAA - \(error)")
        }
    }

    override func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
        logger.error("AAATESTAAAA - validateDeviceCredential")
    }

    override func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {
        logger.error("AAATESTAAAA - selectWiFiNetwork")
        return .defaultSystemNetwork
    }
    
    override func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {
        logger.error("AAATESTAAAA - selectThreadNetwork")
        let scanResult = threadScanResults[0] // .defaultSystemNetwork doesn't work. Selecting first.
        return ThreadNetworkAssociation.network(extendedPANID: scanResult.extendedPANID)
    }
}
