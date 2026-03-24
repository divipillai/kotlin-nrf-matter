//
//  RequestHandlerProtocol.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 24/03/2026.
//

import MatterSupport

protocol RequestHandlerProtocol {
    func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room]
    
    func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws
    
    func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async
    
    func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws
    
    func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation
    
    func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation
}
