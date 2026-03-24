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
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
          let fetchedRooms = try homeMatterCommissioner.fetchRooms()
          return fetchedRooms
        } catch {
            let rooms: [String] = ["Living Room", "Bedroom", "Office", "Kitchen", "Dining Room"]
            return rooms.map { MatterAddDeviceRequest.Room(displayName: $0) }
        }
    }

    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
        do {
            try await homeMatterCommissioner.commissionMatterDevice(onboardingPayload: onboardingPayload)
        } catch {
            logger.error("\(error)")
        }
    }

    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
          try await homeMatterCommissioner.configureMatterDevice(deviceName: name, roomName: room?.displayName)
        } catch {
            logger.error("\(error)")
        }
    }

    override func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
    }

    override func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {
        return .defaultSystemNetwork
    }
    
    override func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {
        let scanResult = threadScanResults[0] // .defaultSystemNetwork doesn't work. Selecting first.
        return ThreadNetworkAssociation.network(extendedPANID: scanResult.extendedPANID)
    }
}
