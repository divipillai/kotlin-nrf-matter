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

  // MARK: - MTRDeviceAttestationDelegate

  func deviceAttestationCompleted(
    for controller: MTRDeviceController,
    opaqueDeviceHandle: UnsafeMutableRawPointer,
    attestationDeviceInfo: MTRDeviceAttestationDeviceInfo,
    error: (any Error)?
  ) {
    Logger().info("AttestationDelegate - deviceAttestationCompleted (error: \(error)).")
    do {
      try controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure: true)
    } catch {
      Logger().error("AttestationDelegate - failed to continue commissioning device error: \(error).")
    }
  }

  func deviceAttestationFailed(
    for controller: MTRDeviceController,
    opaqueDeviceHandle: UnsafeMutableRawPointer,
    error: any Error
  ) {
    Logger().error("AttestationDelegate - deviceAttestationFailed with error: \(error).")
    do {
      try controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure: true)
    } catch {
      Logger().error("AttestationDelegate - failed to continue commissioning device error: \(error).")
    }
  }
}
