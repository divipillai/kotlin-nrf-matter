//
//  MatterAttestationDelegate.swift
//  iosApp
//
//  Created by Sylwester Zielinski on 27/02/2026.
//

import Foundation
import Matter
import SharedCode

class MatterAttestationDelegate: NSObject, MTRDeviceAttestationDelegate {
    
    // MARK: - MTRDeviceAttestationDelegate
    
    func deviceAttestationCompleted(
        for controller: MTRDeviceController,
        opaqueDeviceHandle: UnsafeMutableRawPointer,
        attestationDeviceInfo: MTRDeviceAttestationDeviceInfo,
        error: (any Error)?
    ) {
        SharedLogger.info("DeviceAttestationCompleted (error: \(error)).")
        do {
            try controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure: true)
        } catch {
            SharedLogger.error("Failed to continue commissioning device error: \(error).")
        }
    }
    
    func deviceAttestationFailed(
        for controller: MTRDeviceController,
        opaqueDeviceHandle: UnsafeMutableRawPointer,
        error: any Error
    ) {
        SharedLogger.error("DeviceAttestationFailed with error: \(error).")
        do {
            try controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure: true)
        } catch {
            SharedLogger.error("Failed to continue commissioning device error: \(error).")
        }
    }
}
