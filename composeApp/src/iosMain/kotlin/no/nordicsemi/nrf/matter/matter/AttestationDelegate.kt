@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.matter

import io.github.aakira.napier.Napier
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.Matter.MTRDeviceAttestationDelegateProtocol
import platform.Matter.MTRDeviceAttestationDeviceInfo
import platform.Matter.MTRDeviceController
import platform.darwin.NSObject

class AttestationDelegate : NSObject(), MTRDeviceAttestationDelegateProtocol {

    override fun deviceAttestationCompletedForController(
        controller: MTRDeviceController,
        opaqueDeviceHandle: COpaquePointer?,
        attestationDeviceInfo: MTRDeviceAttestationDeviceInfo,
        error: NSError?
    ) {
        try {
            Napier.i("AttestationDelegate - continueCommissioningDevice after completed")
            controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure = true, null)
        } catch (t: Throwable) {
            Napier.i("AttestationDelegate - error during continueCommissioningDevice")
        }
    }

    override fun deviceAttestationFailedForController(
        controller: MTRDeviceController,
        opaqueDeviceHandle: COpaquePointer?,
        error: NSError
    ) {
        try {
            Napier.i("AttestationDelegate - continueCommissioningDevice after fail")
            controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure = true, null)
        } catch (t: Throwable) {
            Napier.i("AttestationDelegate - error during continueCommissioningDevice")
        }
    }
}
