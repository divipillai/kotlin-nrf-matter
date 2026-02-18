package no.nordicsemi.nrf.matter.matter

import io.github.aakira.napier.Napier
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Matter.MTRCommissioneeInfo
import platform.Matter.MTRCommissioningStatus
import platform.Matter.MTRDeviceController
import platform.Matter.MTRDeviceControllerDelegateProtocol
import platform.Matter.MTRMetrics
import platform.darwin.NSObject

class MatterControllerDelegate : NSObject(), MTRDeviceControllerDelegateProtocol {
    override fun controller(controller: MTRDeviceController, statusUpdate: MTRCommissioningStatus) {
        Napier.i("Commissioning status $statusUpdate.")
    }

    override fun controller(
        controller: MTRDeviceController,
        commissioningSessionEstablishmentDone: NSError?
    ) {
        Napier.i("Commissioning commissioningSessionEstablishmentDone.")
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
