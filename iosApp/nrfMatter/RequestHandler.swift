//
//  RequestHandler.swift
//  nrfMatter
//
//  Created by Sylwester Zielinski on 24/02/2026.
//

import MatterSupport
import Matter
import shared

/// Entry point class for the Matter "Add Device" app extension.
///
/// The system extension provides the UI that scans the commissioning QR code, then calls back into
/// these overrides: consuming the payload read from the QR code, providing a list of rooms and homes
/// the user may add their device to, and selecting the WiFi or Thread network the device will
/// operate on.
///
/// Every step is delegated to `AppExtensionMatterCommissioner`, which is Kotlin code reached through
/// the `shared` framework — this target holds no commissioning logic of its own. That Kotlin class
/// in turn drives `ios-matter`'s `MatterCommissioner` to add the device to the local Matter fabric.
///
/// The extension runs in its own process, so it exchanges data with the main app through
/// `SharedStorage` (`UserDefaults` over an app group): the app writes the node ID to commission
/// before starting the flow, and ``configureDevice(named:in:)`` writes back the success flag the app
/// reads once the extension closes.
final class RequestHandler: MatterAddDeviceExtensionRequestHandler {
    
    private let commissioner = AppExtensionMatterCommissioner()

    /// Returns the list of rooms available in the given home for placing a newly added device.
    ///
    /// - Parameter home: The home to fetch rooms for. Ignored — the room list is a fixed set.
    /// - Returns: The rooms the device can be assigned to.
    override func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        return commissioner.rooms().map { MatterAddDeviceRequest.Room(displayName: $0) }
    }

    /// Commissions the device described by the onboarding payload into the given home.
    ///
    /// - Parameters:
    ///   - home: The home the device is being added to, or `nil` if no home was selected.
    ///   - onboardingPayload: The Matter onboarding payload read from the commissioning QR code.
    ///   - commissioningID: The unique identifier for this commissioning attempt.
    /// - Throws: An error if commissioning fails.
    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        try await commissioner.commissionDevice(payload: onboardingPayload)
    }

    /// Finishes configuring a newly added device with its chosen name and room, and records the result in shared storage.
    ///
    /// - Parameters:
    ///   - name: The display name chosen for the device.
    ///   - room: The room the device was placed in, or `nil` if no room was selected.
    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        commissioner.configureDevice()
    }

    /// Accepts the device credential presented during commissioning without validating it.
    ///
    /// Returning without throwing tells the system every credential is acceptable, which is what
    /// this app wants: it commissions development kits and simulated devices whose certificates are
    /// not signed by a production attestation authority. ``MatterAttestationDelegate`` takes the
    /// same stance for the attestation step.
    ///
    /// - Parameter deviceCredential: The credential presented by the device. Ignored.
    override func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
    }

    /// Selects a WiFi network for the device to join.
    ///
    /// Always defers to the network the phone is already on, so the scan results are unused.
    ///
    /// - Parameter wifiScanResults: The WiFi networks discovered during scanning. Ignored.
    /// - Returns: `.defaultSystemNetwork`.
    override func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {

        return .defaultSystemNetwork
    }

    /// Selects a Thread network for the device to join, from the networks found during scanning.
    ///
    /// Logs every network found, then picks the first one by extended PAN ID.
    /// `.defaultSystemNetwork` is not usable here, so there is no way to defer the choice to the
    /// system.
    ///
    /// - Parameter threadScanResults: The Thread networks discovered during scanning. Must not be
    ///   empty — the first entry is read unconditionally.
    /// - Returns: An association naming the first scanned network's extended PAN ID.
    override func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {

        let networkNames = threadScanResults.map { $0.networkName }
        commissioner.onThreadNetworksDetected(names: networkNames)

        let scanResult = threadScanResults[0] // .defaultSystemNetwork doesn't work. Selecting first.
        return MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation.network(extendedPANID: scanResult.extendedPANID)
    }
}
