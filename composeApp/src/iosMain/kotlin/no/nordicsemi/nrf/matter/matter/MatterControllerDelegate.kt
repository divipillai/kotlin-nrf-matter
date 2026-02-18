@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.matter

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Matter.MTRCommissioneeInfo
import platform.Matter.MTRCommissioningParameters
import platform.Matter.MTRCommissioningStatus
import platform.Matter.MTRDevice
import platform.Matter.MTRDeviceController
import platform.Matter.MTRDeviceControllerDelegateProtocol
import platform.Matter.MTRMetrics
import platform.darwin.NSObject

sealed interface MatterControllerResult {
    object Failure: MatterControllerResult
    data class Success(val device: MTRDevice) : MatterControllerResult
}

class MatterControllerDelegate(private val nodeId: NSNumber) : NSObject(), MTRDeviceControllerDelegateProtocol {
    val result = MutableStateFlow<MatterControllerResult?>(null)

    override fun controller(controller: MTRDeviceController, statusUpdate: MTRCommissioningStatus) {
        Napier.i("Commissioning status $statusUpdate.")
    }

    override fun controller(
        controller: MTRDeviceController,
        commissioningSessionEstablishmentDone: NSError?
    ) {
        Napier.i("Commissioning commissioningSessionEstablishmentDone.")

        try {
            val params = MTRCommissioningParameters()
            params.deviceAttestationDelegate = AttestationDelegate()
            controller.commissionNodeWithID(nodeId, params, null)
            val device = controller.devices.map { it as MTRDevice }.first { it.nodeID == nodeId }
            result.tryEmit(MatterControllerResult.Success(device))
        } catch (t: Throwable) {
            Napier.i("Commissioning node failed.")
        }
    }

    override fun controller(
        controller: MTRDeviceController,
        commissioningComplete: NSError?,
        nodeID: NSNumber?,
        metrics: MTRMetrics
    ) {
        Napier.i("Commissioning commissioningComplete.")
    }

    override fun controller(
        controller: MTRDeviceController,
        commissioneeHasReceivedNetworkCredentials: NSNumber
    ) {
        Napier.i("Commissioning commissioneeHasReceivedNetworkCredentials.")
    }

    override fun controller(
        controller: MTRDeviceController,
        commissioningComplete: NSError?,
        nodeID: NSNumber?
    ) {
        Napier.i("Commissioning commissioningComplete.")
    }

    override fun controller(
        controller: MTRDeviceController,
        readCommissioneeInfo: MTRCommissioneeInfo
    ) {
        Napier.i("Commissioning readCommissioneeInfo.")
    }

    override fun controller(controller: MTRDeviceController, suspendedChangedTo: Boolean) {
        Napier.i("Commissioning suspendedChangedTo: $suspendedChangedTo.")
    }
}
