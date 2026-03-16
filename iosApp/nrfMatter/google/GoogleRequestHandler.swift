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

final class GoogleRequestHandler: MatterAddDeviceExtensionRequestHandler {

    override func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
          let fetchedRooms = try homeMatterCommissioner.fetchRooms()
          // Returning fetched rooms.
          return fetchedRooms
        } catch {
          // Failed to fetch rooms with error
          return []
        }
    }

    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        var onboardingPayloadForHub = onboardingPayload
        let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
        try await homeMatterCommissioner.commissionMatterDevice(onboardingPayload: onboardingPayloadForHub)
    }

    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        do {
          let homeMatterCommissioner = try HomeMatterCommissioner(appGroup: SharedConsts.appGroup)
          try await homeMatterCommissioner.configureMatterDevice(deviceName: name, roomName: room?.displayName)
        } catch {
          // Configure Device failed with error
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
