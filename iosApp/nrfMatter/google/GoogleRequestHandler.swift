//
//  GoogleRequestHandler.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 16/03/2026.
//

import GoogleHomeSDK
import MatterSupport
import Matter
import OSLog
import SharedCode

final class GoogleRequestHandler: RequestHandlerProtocol {

    func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.googleStorage)
          let fetchedRooms = try homeMatterCommissioner.fetchRooms()
          return fetchedRooms
        } catch {
            let rooms: [String] = ["Living Room", "Bedroom", "Office", "Kitchen", "Dining Room"]
            return rooms.map { MatterAddDeviceRequest.Room(displayName: $0) }
        }
    }

    func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.googleStorage)
        do {
            try await homeMatterCommissioner.commissionMatterDevice(onboardingPayload: onboardingPayload)
        } catch {
            SharedLogger.error("\(error)")
        }
    }

    func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.googleStorage)
          try await homeMatterCommissioner.configureMatterDevice(deviceName: name, roomName: room?.displayName)
        } catch {
            SharedLogger.error("\(error)")
        }
    }

    func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
    }

    func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {
        return .defaultSystemNetwork
    }
    
    func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {
        let scanResult = threadScanResults[0] // .defaultSystemNetwork doesn't work. Selecting first.
        return MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation.network(extendedPANID: scanResult.extendedPANID)
    }
}
