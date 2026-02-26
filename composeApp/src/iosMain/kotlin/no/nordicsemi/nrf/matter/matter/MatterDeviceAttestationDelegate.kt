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

class MatterDeviceAttestationDelegate : NSObject(), MTRDeviceAttestationDelegateProtocol {

    override fun deviceAttestation(
        controller: MTRDeviceController,
        completedForDevice: COpaquePointer?,
        attestationDeviceInfo: MTRDeviceAttestationDeviceInfo,
        error: NSError?
    ) {
        try {
            Napier.i("BBBTESTAAA - completedForDevice")
            controller.continueCommissioningDevice(completedForDevice, ignoreAttestationFailure = true, null)
        } catch (t: Throwable) {
            Napier.i("BBBTESTAAA - completedForDevice error")
        }
    }

    override fun deviceAttestationCompletedForController(
        controller: MTRDeviceController,
        opaqueDeviceHandle: COpaquePointer?,
        attestationDeviceInfo: MTRDeviceAttestationDeviceInfo,
        error: NSError?
    ) {
        try {
            Napier.i("BBBTESTAAA - deviceAttestationCompletedForController")
            controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure = true, null)
        } catch (t: Throwable) {
            Napier.i("BBBTESTAAA - deviceAttestationCompletedForController error")
        }
    }

    override fun deviceAttestationFailedForController(
        controller: MTRDeviceController,
        opaqueDeviceHandle: COpaquePointer?,
        error: NSError
    ) {
        try {
            Napier.i("BBBTESTAAA - deviceAttestationFailedForController")
            controller.continueCommissioningDevice(opaqueDeviceHandle, ignoreAttestationFailure = true, null)
        } catch (t: Throwable) {
            Napier.i("BBBTESTAAA - deviceAttestationFailedForController error")
        }
    }

    override fun deviceAttestation(
        controller: MTRDeviceController,
        failedForDevice: COpaquePointer?,
        error: NSError
    ) {
        try {
            Napier.i("BBBTESTAAA - failedForDevice")
            controller.continueCommissioningDevice(failedForDevice, ignoreAttestationFailure = true, null)
        } catch (t: Throwable) {
            Napier.i("BBBTESTAAA - failedForDevice error")
        }
    }
}