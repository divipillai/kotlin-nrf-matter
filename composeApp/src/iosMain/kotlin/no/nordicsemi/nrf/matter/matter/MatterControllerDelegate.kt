@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.matter

import io.github.aakira.napier.Napier
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.nrf.matter.ThreadNetwork
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Matter.MTRCommissioneeInfo
import platform.Matter.MTRCommissioningParameters
import platform.Matter.MTRCommissioningStatus
import platform.Matter.MTRDevice
import platform.Matter.MTRDeviceAttestationDelegateProtocol
import platform.Matter.MTRDeviceAttestationDeviceInfo
import platform.Matter.MTRDeviceController
import platform.Matter.MTRDeviceControllerDelegateProtocol
import platform.Matter.MTRMetrics
import platform.darwin.NSObject

sealed interface MatterControllerResult {
    object Failure: MatterControllerResult
    data class Success(val device: MTRDevice) : MatterControllerResult
}

class MatterControllerDelegate(private val nodeId: NSNumber, private val threadNetwork: ThreadNetwork?) : NSObject(), MTRDeviceControllerDelegateProtocol {
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
            controller.commissionNodeWithID() ?: throw IllegalStateException("Couldn't commission node.")
            Napier.i("Successfully commissioned node with id.")
        } catch (t: Throwable) {
            Napier.e("Commissioning node failed.", throwable = t)
        }
    }

    private fun MTRDeviceController.commissionNodeWithID(): MTRDeviceController? = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val params = MTRCommissioningParameters()
        params.deviceAttestationDelegate = MatterDeviceAttestationDelegate()
        Napier.i("Commision node with id: $threadNetwork")
//        threadNetwork?.data?.let {
//            params.threadOperationalDataset = it
//            params.deviceAttestationDelegate = MatterDeviceAttestationDelegate()
//        }
        commissionNodeWithID(nodeId, params, error.ptr)

        if (error.value != null) {
            Napier.e("Couldn't commission node: ${error.value}")
            return null
        }

        return this@commissionNodeWithID
    }

    override fun controller(
        controller: MTRDeviceController,
        commissioningComplete: NSError?,
        nodeID: NSNumber?,
        metrics: MTRMetrics
    ) {
        if (commissioningComplete != null || nodeID == null) {
            Napier.i("Commissioning error: $commissioningComplete")
        } else {
            Napier.i("Commissioning commissioningComplete with metrics: $nodeID.")
            val device = MTRDevice.deviceWithNodeID(nodeID, controller)
//            MatterDevicesProvider.saveDevice(device)
            Napier.i("Sending success")
            result.tryEmit(MatterControllerResult.Success(device))
        }
    }

    override fun devicesChangedForController(controller: MTRDeviceController) {
        Napier.i("devicesChangedForController")
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
        if (commissioningComplete != null) {
            Napier.i("Commissioning error: $commissioningComplete")
        } else {
            Napier.i("Commissioning commissioningComplete: $nodeID.")
        }
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

    override fun finalize() {
        Napier.i("Finalize delegate.")
    }
}
