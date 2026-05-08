//
//  LocalRequestHandler.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 16/03/2026.
//

import MatterSupport
import Matter
import OSLog
import SharedCode

final class LocalRequestHandler: RequestHandlerProtocol {
    
    private let commissioner = MatterCommissioner()
    
    func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        let rooms: [String] = ["Living Room", "Bedroom", "Office", "Kitchen", "Dining Room"]
        return rooms.map { MatterAddDeviceRequest.Room(displayName: $0) }
    }

    func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        try await commissioner.commission(payload: onboardingPayload, nodeID: NodeIdProvider.id)  // todo
    }

    func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        commissioner.release()
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
