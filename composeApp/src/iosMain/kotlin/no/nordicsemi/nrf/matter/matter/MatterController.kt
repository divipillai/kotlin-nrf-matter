@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.matter

import io.github.aakira.napier.Napier
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import no.nordicsemi.nrf.matter.model.Device
import platform.Foundation.NSError
import platform.Foundation.NSMutableData
import platform.Foundation.NSNumber
import platform.Foundation.create
import platform.Matter.MTRDeviceController
import platform.Matter.MTRDeviceControllerFactory
import platform.Matter.MTRDeviceControllerFactoryParams
import platform.Matter.MTRDeviceControllerStartupParams
import platform.Matter.MTRSetupPayload
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.darwin.dispatch_queue_create

val nodeID = NSNumber(1)

object MatterController {

    suspend fun commission(code: String, onError: () -> Unit): Device? {
        try {
            return commission(code)
        } catch (t: Throwable) {
            onError()
            return null
        }
    }

    private suspend fun commission(code: String): Device {
        Napier.i("Initializing Matter controller factory.")
        val storage = MatterStorage()
        val factoryParams = MTRDeviceControllerFactoryParams(storage)
        val factory = MTRDeviceControllerFactory.sharedInstance().startControllerFactory(factoryParams)
            ?: throw IllegalStateException("Couldn't start Matter controller factory.")
        Napier.i("Matter controller factory successfully initialized.")

        val ipk = NSMutableData.create(length = 16u)!!
        val status = SecRandomCopyBytes(kSecRandomDefault, ipk.length, ipk.mutableBytes)
        if (status != errSecSuccess) {
            throw IllegalStateException("Error during copy bytes.")
        }

        val params = MTRDeviceControllerStartupParams(
            iPK = ipk,
            fabricID = NSNumber(1),
            nocSigner = MatterKeypair(),
        )
//            params.vendorID = NSNumber(0x127F)
        params.vendorID = NSNumber(0x1234)
//        params.operationalCertificateIssuer = MatterCertificateIssuer()
//        params.operationalCertificateIssuerQueue = dispatch_queue_create("no.nordicsemi.nrf.matter.certissuer", null)

        Napier.i("Creating Matter controller.")
        val controller: MTRDeviceController = factory.createControllerOnNewFabric(params)
            ?: factory.createControllerOnExistingFabric(params)
            ?: throw IllegalStateException("Couldn't create a controller")
        Napier.i("Matter controller successfully created.")

        Napier.i("Opening Matter commissioning session.")
        val delegate = MatterControllerDelegate(nodeID)
        controller.setDeviceControllerDelegate(delegate, dispatch_queue_create("no.nordicsemi.nrf.matter.controller", null))

        controller.setupCommissioningSessionWithPayload(code)
            ?: throw IllegalStateException("Couldn't start commissioning session.")
        Napier.i("Matter commissioning session successfully opened.")

        val successResult = delegate.result.filterIsInstance<MatterControllerResult.Success>().first()
        Napier.i("Received success")
        with (successResult.device) {
            return Device(
                vendorName = "Nordic Semiconductor", // TODO
                productName = "nRF54",
                vendorId = vendorID?.stringValue,
                productId = productID?.stringValue,
                deviceId = nodeID.longValue,
                name = "Matter device",
                deviceMatterInfo = listOf()
            )
        }
    }

    private fun MTRDeviceController.setupCommissioningSessionWithPayload(code: String): MTRDeviceController? = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val payload = MTRSetupPayload(payload = code)
        setupCommissioningSessionWithPayload(payload = payload, newNodeID = nodeID, error = error.ptr)

        if (error.value != null) {
            Napier.e("Couldn't start commissioning session: ${error.value}")
            return null
        }

        return this@setupCommissioningSessionWithPayload
    }

    private fun MTRDeviceControllerFactory.startControllerFactory(params: MTRDeviceControllerFactoryParams): MTRDeviceControllerFactory? = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        startControllerFactory(params, error.ptr)

        if (error.value != null) {
            Napier.e("Couldn't create controller on a new fabric: ${error.value}")
            return null
        }

        return this@startControllerFactory
    }

    private fun MTRDeviceControllerFactory.createControllerOnNewFabric(params: MTRDeviceControllerStartupParams): MTRDeviceController? = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val result = createControllerOnNewFabric(params, error = error.ptr)

        if (result == null) {
            Napier.e("Couldn't create controller on a new fabric: ${error.value}")
        }

        return result
    }

    private fun MTRDeviceControllerFactory.createControllerOnExistingFabric(params: MTRDeviceControllerStartupParams): MTRDeviceController? = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val result = createControllerOnExistingFabric(params, error = error.ptr)

        if (result == null) {
            Napier.e("Couldn't create controller on the existing fabric: ${error.value}")
        }
        return result
    }
}
