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
        Napier.i("BBBTESTAAA - completedForDevice")
    }

    override fun deviceAttestationCompletedForController(
        controller: MTRDeviceController,
        opaqueDeviceHandle: COpaquePointer?,
        attestationDeviceInfo: MTRDeviceAttestationDeviceInfo,
        error: NSError?
    ) {
        Napier.i("BBBTESTAAA - deviceAttestationCompletedForController")
    }

    override fun deviceAttestationFailedForController(
        controller: MTRDeviceController,
        opaqueDeviceHandle: COpaquePointer?,
        error: NSError
    ) {
        Napier.i("BBBTESTAAA - deviceAttestationFailedForController")
    }

    override fun deviceAttestation(
        controller: MTRDeviceController,
        failedForDevice: COpaquePointer?,
        error: NSError
    ) {
        Napier.i("BBBTESTAAA - failedForDevice")
    }
}