//
//  RequestHandler.swift
//  nrfMatter
//
//  Created by Sylwester Zielinski on 24/02/2026.
//

import MatterSupport
import Matter
import os.log
import ComposeApp

final class RequestHandler: MatterAddDeviceExtensionRequestHandler {
    // Define an error type for pairing failures.
    enum PairingError: Error {
        case invalidCredentials
        case pairingFailed
    }

    // Use OSLog to log debugging information.
    private let logger = Logger(subsystem: "com.yourcompany.matterapp", category: "DeviceSetup")

    override init() {
        super.init()
        logger.debug("AAATESTAAA - MatterAddDeviceExtensionRequestHandler initialized")
    }


    // Override this method to return the rooms in the home.
    override func rooms(in home: MatterAddDeviceRequest.Home?) async -> [MatterAddDeviceRequest.Room] {
        logger.debug("Received request to fetch rooms in home: \(String(describing: home?.displayName)).")


        // In your app, fetch rooms from your database or ecosystem.
        let rooms: [String] = ["Living Room", "Bedroom", "Office", "Kitchen", "Dining Room", "AAATESTAAA"]
        return rooms.map { MatterAddDeviceRequest.Room(displayName: $0) }
    }


    // Override this method to commission the device to your application.
    override func commissionDevice(in home: MatterAddDeviceRequest.Home?, onboardingPayload: String, commissioningID: UUID) async throws {
        logger.debug("Commissioning device in home '\(String(describing: home?.displayName))' with payload: \(onboardingPayload).")


        let commissioner = MatterCommissioner()
        try await commissioner.commision(payload: onboardingPayload)
//        do {
//            // Parse the onboarding payload and commission the device to your app using the Matter framework APIs.
//            logger.info("Successfully commissioned device with ID: \(commissioningID)")
//
//
//            // Make sure that your application returns from this method when it finished pairing the accessory with your application.
//            // Returning from this method indicates to the MatterSupport framework that the pairing process is completed,
//
//
//            // and the system displays a view to indicate that the pairing process is completed.
//        } catch {
//            logger.error("Failed to commission device: \(error.localizedDescription)")
//            throw PairingError.pairingFailed
//        }
    }


    // Override this method to configure the device to your application.
    override func configureDevice(named name: String, in room: MatterAddDeviceRequest.Room?) async {
        logger.debug("Configuring device '\(name)' in room: \(String(describing: room?.displayName))")


        // Retrieve and configure the newly paired device in your ecosystem;
        // for example, find the device, set its name or room, apply default configurations, and save information in your database.
        logger.info("Device '\(name)' successfully configured")
    }


    // Override this method to validate the device's credentials.
    override func validateDeviceCredential(_ deviceCredential: MatterAddDeviceExtensionRequestHandler.DeviceCredential) async throws {
        logger.debug("Validating device credential")


        // This code snippet skips validation for better readability.
        // Make sure to replace the following line with actual validation code.
        let isValid = true


        if !isValid {
            logger.warning("Device credential validation failed")
            throw PairingError.invalidCredentials
        } else {
            logger.info("Device credential successfully validated")
        }
    }


    // Override this method to select a specific Wi-Fi network or to ask the Matter framework to select the default WiFi network.
    override func selectWiFiNetwork(from wifiScanResults: [MatterAddDeviceExtensionRequestHandler.WiFiScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.WiFiNetworkAssociation {
        logger.debug("Selecting WiFi network from \(wifiScanResults.count) scan results")

        // Check if a specific network is available.
        let preferredSSID = "YourPreferredNetwork"
        let preferredNetwork = wifiScanResults.first(where: { result in
//            result.ssid == preferredSSID
            String(data: result.ssid, encoding: .utf8) == preferredSSID && result.security != .unencrypted
        })

//        if let network = preferredNetwork {
//            // Use stored credentials or prompt the person for their password.
//            let credentials = "YourSecurePassword"
//            logger.info("Selected specific WiFi network: \(network.ssid)")
//            return .network(ssid: network.ssid, credentials: credentials)
//        } else {
//            // Use the system's default network.
//            logger.info("Using default system WiFi network.")
//
//        }
        return .defaultSystemNetwork
    }


    // Override this method to select a specific Thread network or to ask the Matter framework to select the default Thread network.
    override func selectThreadNetwork(from threadScanResults: [MatterAddDeviceExtensionRequestHandler.ThreadScanResult]) async throws -> MatterAddDeviceExtensionRequestHandler.ThreadNetworkAssociation {
        logger.debug("Selecting Thread network from \(threadScanResults.count) scan results")


        // Check if a specific network is available by name.
        let preferredNetworkName = "HomeThread"
        let preferredNetwork = threadScanResults.first { result in
            result.networkName == preferredNetworkName
        }


//        if let network = preferredNetwork, let extendedPANID = network.extendedPANID {
//            logger.info("Selected specific Thread network: \(network.networkName ?? "Unnamed")")
//            return .network(extendedPANID: extendedPANID)
//        } else {
//            // Use the system's default Thread network.
//            logger.info("Using default system Thread network")
//            return .defaultSystemNetwork
//        }
        return .defaultSystemNetwork
    }
}
