//
//  MatterAttestationDelegate.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 27/02/2026.
//

import Foundation
import Matter
import OSLog

class MatterAttestationDelegate: NSObject, MTRDeviceAttestationDelegate {
    
    private let logger = Logger(subsystem: "nrf.matter", category: "MatterAttestationDelegate")
    
    // MARK: - MTRDeviceAttestationDelegate
    
    func deviceAttestationCompleted(
        for controller: MTRDeviceController,
        opaqueDeviceHandle: UnsafeMutableRawPointer,
        attestationDeviceInfo: MTRDeviceAttestationDeviceInfo,
        error: (any Error)?
    ) {
        logger.info("DeviceAttestationCompleted (error: \(error)).")
        do {
            try controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure: true)
        } catch {
            logger.error("Failed to continue commissioning device error: \(error).")
        }
    }
    
    func deviceAttestationFailed(
        for controller: MTRDeviceController,
        opaqueDeviceHandle: UnsafeMutableRawPointer,
        error: any Error
    ) {
        logger.error("DeviceAttestationFailed with error: \(error).")
        do {
            try controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure: true)
        } catch {
            logger.error("Failed to continue commissioning device error: \(error).")
        }
    }
}
