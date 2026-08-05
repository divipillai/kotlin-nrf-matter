//
//  RequestHandlerProtocol.swift
//  ios-matter
//
//  Created by Sylwester Zielinski on 24/03/2026.
//

import MatterSupport

/// Abstracts the steps of the Matter "Add Device" flow so that different backends (e.g. local
/// commissioning) can implement how a device is added, configured, and connected to a network.
public protocol RequestHandlerProtocol {
    
    /// Returns the list of rooms available in the given home for placing a newly added device.
    ///
    /// - Parameter home: The home to fetch rooms for, or `nil` if no home was selected.
    /// - Returns: The rooms the device can be assigned to.
    func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room]

    /// Commissions the device described by the onboarding payload into the given home.
    ///
    /// - Parameters:
    ///   - home: The home the device is being added to, or `nil` if no home was selected.
    ///   - onboardingPayload: The Matter onboarding payload read from the commissioning QR code.
    ///   - commissioningID: The unique identifier for this commissioning attempt.
    /// - Throws: An error if commissioning fails.
    func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws

    /// Finishes configuring a newly added device with its chosen name and room.
    ///
    /// - Parameters:
    ///   - name: The display name chosen for the device.
    ///   - room: The room the device was placed in, or `nil` if no room was selected.
    func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async

    /// Validates the device credential presented during commissioning.
    ///
    /// - Parameter deviceCredential: The credential to validate.
    /// - Throws: An error if the credential is invalid.
    func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws

    /// Selects a WiFi network for the device to join, from the networks found during scanning.
    ///
    /// - Parameter wifiScanResults: The WiFi networks discovered during scanning.
    /// - Returns: The network association the device should use.
    /// - Throws: An error if no suitable network can be selected.
    func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation

    /// Selects a Thread network for the device to join, from the networks found during scanning.
    ///
    /// - Parameter threadScanResults: The Thread networks discovered during scanning.
    /// - Returns: The network association the device should use.
    /// - Throws: An error if no suitable network can be selected.
    func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation
}
