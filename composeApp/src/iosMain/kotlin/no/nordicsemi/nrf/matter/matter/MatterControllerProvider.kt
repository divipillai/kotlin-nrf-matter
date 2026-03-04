@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.matter

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError
import platform.Foundation.NSMutableData
import platform.Foundation.NSNumber
import platform.Foundation.create
import platform.Matter.MTRDeviceController
import platform.Matter.MTRDeviceControllerFactory
import platform.Matter.MTRDeviceControllerFactoryParams
import platform.Matter.MTRDeviceControllerStartupParams
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

object MatterControllerProvider {
    fun create(): MTRDeviceController {
        Napier.i("Initializing Matter controller factory.")
        val storage = MatterStorage()
        val factoryParams = MTRDeviceControllerFactoryParams(storage)
        val factory =
            MTRDeviceControllerFactory.sharedInstance().startControllerFactory(factoryParams)
                ?: throw IllegalStateException("Couldn't start Matter controller factory.")
        Napier.i("Matter controller factory successfully initialized.")

        Napier.i("AAATESTAAA - known fabrics: ${factory.knownFabrics?.size}")

        val ipk = NSMutableData.create(length = 16u)!!
        val status = SecRandomCopyBytes(kSecRandomDefault, ipk.length, ipk.mutableBytes)
        if (status != errSecSuccess) {
            throw IllegalStateException("Error during copy bytes.")
        }

        val keypair = MatterKeypair()

        Napier.i("CCCTESTCCC - ipk: $ipk")
        Napier.i("CCCTESTCCC - private key: ${keypair.privateKey.asString()}")
        Napier.i("CCCTESTCCC - public key: ${keypair.publicKey.asString()}")

        val params = MTRDeviceControllerStartupParams(
            iPK = ipk,
            fabricID = NSNumber(1),
            nocSigner = keypair,
        )
//            params.vendorID = NSNumber(0x127F)
        params.vendorID = NSNumber(0xFFF1)
//        params.operationalCertificateIssuer = MatterCertificateIssuer()
//        params.operationalCertificateIssuerQueue = dispatch_queue_create("no.nordicsemi.nrf.matter.certissuer", null)

        Napier.i("Creating Matter controller.")
        val controller: MTRDeviceController = factory.createControllerOnExistingFabric(params)
            ?: factory.createControllerOnNewFabric(params)
            ?: throw IllegalStateException("Couldn't create a controller")
        Napier.i("Matter controller successfully created.")

        return controller
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
        Napier.e("Create controller on a new fabric.")
        val error = alloc<ObjCObjectVar<NSError?>>()
        val result = createControllerOnNewFabric(params, error = error.ptr)

        if (result == null) {
            Napier.e("Couldn't create controller on a new fabric: ${error.value}")
        }

        return result
    }

    private fun MTRDeviceControllerFactory.createControllerOnExistingFabric(params: MTRDeviceControllerStartupParams): MTRDeviceController? = memScoped {
        Napier.e("Create controller on the existing fabric.")
        val error = alloc<ObjCObjectVar<NSError?>>()
        val result = createControllerOnExistingFabric(params, error = error.ptr)

        if (result == null) {
            Napier.e("Couldn't create controller on the existing fabric: ${error.value}")
        }
        return result
    }
}
