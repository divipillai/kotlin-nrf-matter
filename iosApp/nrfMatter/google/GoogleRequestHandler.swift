//
//  GoogleRequestHandler.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 16/03/2026.
//


import MatterSupport
import Matter
import OSLog
import SharedCode

final class GoogleRequestHandler: MatterAddDeviceExtensionRequestHandler {

    override func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        let rooms: [String] = ["Living Room", "Bedroom", "Office", "Kitchen", "Dining Room"]
        return rooms.map { MatterAddDeviceRequest.Room(displayName: $0) }
    }

    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {

    }

    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {

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
