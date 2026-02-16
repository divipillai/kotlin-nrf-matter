package no.nordicsemi.nrf.matter.matter

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.Foundation.NSMutableData
import platform.Foundation.NSNumber
import platform.Foundation.create
import platform.Matter.MTRCommissioningStatus
import platform.Matter.MTRDeviceController
import platform.Matter.MTRDeviceControllerDelegateProtocol
import platform.Matter.MTRDeviceControllerFactory
import platform.Matter.MTRDeviceControllerFactoryParams
import platform.Matter.MTRDeviceControllerStartupParams
import platform.Matter.MTRMetrics
import platform.Matter.MTRSetupPayload
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.darwin.NSObject

object MatterExample {

    @OptIn(ExperimentalForeignApi::class)
    fun commision() {
        val factory = MTRDeviceControllerFactory.sharedInstance()

        val storage = MatterStorage()
        val factoryParams = MTRDeviceControllerFactoryParams(storage)

        try {
            factory.startControllerFactory(factoryParams, null)
        } catch(t: Throwable) {
             TODO()
        }

        val ipk = NSMutableData.create(length = 16u)!!

        val status = SecRandomCopyBytes(kSecRandomDefault, ipk.length, ipk.mutableBytes)

        if (status != errSecSuccess) {
            TODO()
        } else {
            val params = MTRDeviceControllerStartupParams(
                iPK = ipk,
                fabricID = NSNumber(1),
                nocSigner = MatterKeypair(),
            )

            var controller: MTRDeviceController? = null
            controller = try {
                factory.createControllerOnNewFabric(params, error = null)
            } catch(t: Throwable) {
                try {
                    factory.createControllerOnExistingFabric(params, error = null)
                } catch(t: Throwable) {
                    TODO()
                }
            }

            val myDelegate = MyControllerDelegate()
            controller!!.setDeviceControllerDelegate(myDelegate, null)

            val payload = MTRSetupPayload(payload = "")

            controller.setupCommissioningSessionWithPayload(payload = payload, newNodeID = nodeID, error = null)
        }
    }
}

val nodeID = NSNumber(1)

class MyControllerDelegate : NSObject(), MTRDeviceControllerDelegateProtocol {
    override fun controller(controller: MTRDeviceController, statusUpdate: MTRCommissioningStatus) {

    }

    override fun controller(
        controller: MTRDeviceController,
        commissioningSessionEstablishmentDone: NSError?
    ) {

    }

    override fun controller(
        controller: MTRDeviceController,
        commissioningComplete: NSError?,
        nodeID: NSNumber?,
        metrics: MTRMetrics
    ) {
        Napier
    }
}
