package no.nordicsemi.nrf.matter.matter

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFErrorRef
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSMutableData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.dictionary
import platform.Matter.MTRCertificates
import platform.Matter.MTRCommissioningStatus
import platform.Matter.MTRDeviceAttestationInfo
import platform.Matter.MTRDeviceController
import platform.darwin.NSObject
import platform.Matter.MTRStorageProtocol
import platform.Matter.MTRDeviceControllerFactory
import platform.Matter.MTRDeviceControllerFactoryParams
import platform.Matter.MTRDeviceControllerStartupParams
import platform.Matter.MTROperationalCSRInfo
import platform.Matter.MTROperationalCertificateChain
import platform.Matter.MTROperationalCertificateIssuerProtocol
import platform.Matter.MTRDeviceControllerDelegateProtocol
import platform.Matter.MTRKeypairProtocol
import platform.Matter.MTRMetrics
import platform.Matter.MTRSetupPayload
import platform.Matter.generateOperationalCertificate
import platform.Matter.generateRootCertificate
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.Security.*
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create

class MyStorage : NSObject(), MTRStorageProtocol {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun removeStorageDataForKey(key: String): Boolean {
        defaults.removeObjectForKey(key)
        return defaults.synchronize()
    }

    override fun storageDataForKey(key: String): NSData? {
        return defaults.dataForKey(key)
    }

    override fun setStorageData(value: NSData, forKey: String): Boolean {
        defaults.setObject(value, forKey)
        return defaults.synchronize()
    }
}

@ExperimentalForeignApi
class MyKeypair(private val privateKey: SecKeyRef) : NSObject(), MTRKeypairProtocol {

    override fun publicKey(): SecKeyRef {
        return SecKeyCopyPublicKey(privateKey)!!
    }

    override fun signMessageECDSA_DER(message: NSData): NSData {
        var error: CFErrorRef? = null

        // Create the signature using the private key
        val signature = SecKeyCreateSignature(
            privateKey,
            kSecKeyAlgorithmECDSASignatureMessageX962SHA256,
            message as CFDataRef,
            null
        ) as NSData?

        if (signature == null) {
            throw IllegalStateException("Failed to sign message")
        }
        return signature
    }
}

@ExperimentalForeignApi
class MyCertificateIssuer(
    private val rootKeypair: MTRKeypairProtocol,
    private val rootCertificate: NSData
) : NSObject(), MTROperationalCertificateIssuerProtocol {

    override fun issueOperationalCertificateForRequest(
        csrInfo: MTROperationalCSRInfo,
        attestationInfo: MTRDeviceAttestationInfo,
        controller: MTRDeviceController,
        completion: (MTROperationalCertificateChain?, NSError?) -> Unit
    ) {
        // Use the Matter framework helper to generate the certificate
        // We sign the new node's CSR using our Root Keypair
        val newCertificate = MTRCertificates.generateOperationalCertificate(
            pk = csrInfo.csrNonce, // Assuming CSR nonce usage or extracting public key from CSR blob
            signingKeypair = rootKeypair,
            signingCertificate = rootCertificate,
            nodeID = NSNumber(unsignedLongLong = 1234u), // Assign a Node ID for the new device
            fabricID = NSNumber(unsignedLongLong = 1u)
        )

        if (newCertificate != null) {
            val chain = MTROperationalCertificateChain(
                operationalCertificate = newCertificate,
                intermediateCertificate = null,
                rootCertificate = rootCertificate,
                adminSubject = null
            )
            completion(chain, null)
        } else {
            // Return an error if generation failed
            completion(null, NSError(domain = "MyIssuer", code = 1, userInfo = null))
        }
    }

    override fun shouldSkipAttestationCertificateValidation(): Boolean {
        // For development, you might skip validation. For prod, set to false.
        return true
    }
}

@ExperimentalForeignApi
fun generateSecureKey(): SecKeyRef {
    val attributes = NSMutableDictionary.dictionary()

    // Klucz typu Elliptic Curve
    attributes.setValue(kSecAttrKeyTypeECSECPrimeRandom, forKey = kSecAttrKeyType)
    // Rozmiar 256 bitów (wymagane przez Matter)
    attributes.setValue(256, forKey = kSecAttrKeySizeInBits)
    // Chcemy, aby klucz prywatny był trwały (opcjonalne, ale przydatne) lub tymczasowy
    attributes.setValue(kSecAttrAccessibleAfterFirstUnlock, forKey = kSecAttrTokenID)

    // Generowanie klucza
    val privateKey = SecKeyCreateRandomKey(attributes as CFDictionaryRef, null)

    return privateKey ?: throw IllegalStateException("Nie udało się wygenerować klucza SecKey")
}

object Aaa {
    @OptIn(ExperimentalForeignApi::class)
    fun commision() {
        val factory = MTRDeviceControllerFactory.sharedInstance()
        val storage = MyStorage()
        val factoryParams = MTRDeviceControllerFactoryParams(storage)

        try {
            factory.startControllerFactory(factoryParams, null)
        } catch(t: Throwable) {
            println("Error starting factory: $t")
            return
        }

        // --- 1. Generate/Load Root CA ---
        // Create a keypair for the Root CA
        val rootKey = MTRCertificates.generateRootCertificateSigningKey()
        val rootKeypair = MyKeypair(rootKey)

        // Generate the Root Certificate
        val rootCertificate = MTRCertificates.generateRootCertificate(
            rootKeypair,
            issuerId = NSNumber(unsignedLongLong = 1u),
            fabricId = NSNumber(unsignedLongLong = 1u),
            error = null
        )!!

        // --- 2. Generate/Load Controller Identity ---
        // Create a keypair for this Controller
        val controllerKey = MTRCertificates.generateOperationalCertificateSigningKey()
        val myKeypair = MyKeypair(controllerKey)

        // Generate the Operational Certificate for the Controller (signed by Root)
        val operationalCertificate = MTRCertificates.generateOperationalCertificate(
            myKeypair,
            signingKeypair = rootKeypair,
            signingCertificate = rootCertificate,
            nodeID = NSNumber(unsignedLongLong = 1u), // Controller is Node 1
            fabricID = NSNumber(unsignedLongLong = 1u),
            error = null
        )!!

        // --- 3. Setup IPK ---
        val ipk = NSMutableData.create(length = 16u)!!
        val status = SecRandomCopyBytes(kSecRandomDefault, ipk.length, ipk.mutableBytes)

        if (status != errSecSuccess) {
            println("Error generating IPK")
            return
        }

        // --- 4. Initialize Params ---
        val params = MTRDeviceControllerStartupParams(
            ipk = ipk,
            operationalKeypair = myKeypair,
            operationalCertificate = operationalCertificate,
            intermediateCertificate = null,
            rootCertificate = rootCertificate
        )

        // Pass the Root Key to the issuer so it can sign NEW devices
        params.operationalCertificateIssuer = MyCertificateIssuer(rootKeypair, rootCertificate)
        params.operationalCertificateIssuerQueue = dispatch_queue_create("Certificate issuer queue", null)

        // --- 5. Start Controller ---
        var controller: MTRDeviceController? = null
        try {
            controller = factory.createControllerOnNewFabric(params, error = null)
        } catch(t: Throwable) {
            try {
                controller = factory.createControllerOnExistingFabric(params, error = null)
            } catch(t: Throwable) {
                println("Failed to create controller")
                return
            }
        }

        val myDelegate = MyControllerDelegate()
        controller!!.setDeviceControllerDelegate(myDelegate, dispatch_get_main_queue())

        // --- 6. Start Commissioning ---
        // Ensure you have a valid Onboarding Payload (QR code string)
        val payloadStr = "MT:..."
        try {
            val payload = MTRSetupPayload(payloadStr)
            controller.setupCommissioningSessionWithPayload(payload, newNodeID = NSNumber(int = 2), error = null)
        } catch (e: Exception) {
            println("Invalid payload")
        }
    }
}

object Aaa {

    @OptIn(ExperimentalForeignApi::class)
    fun commision() {
        // Obtain the controller factory.
        val factory = MTRDeviceControllerFactory.sharedInstance()

        val storage = MyStorage()
        val factoryParams = MTRDeviceControllerFactoryParams(storage)

        try {
            // Start the controller factory.
            factory.startControllerFactory(factoryParams, null)
        } catch(t: Throwable) {
                // Handle any errors.
        }

        val ipk = NSMutableData.create(length = 16u)!!

        val status = SecRandomCopyBytes(kSecRandomDefault, ipk.length, ipk.mutableBytes)

        if (status != errSecSuccess) {
            // Handle any errors.
        } else {
            val params2 = MTRDeviceControllerStartupParams(
//                fabricId = 1,
//                vendorId = 0xFFF1
            )
            val params = MTRDeviceControllerStartupParams(
                iPK = ipk,
                operationalKeypair = myKeypair,
                operationalCertificate = operationalCertificate,
                intermediateCertificate = null,
                rootCertificate = rootCertificate
            )
            params.operationalCertificateIssuer = MyCertificateIssuer()
            params.operationalCertificateIssuerQueue = DispatchQueue(label: "Certificate issuer queue")

            var controller: MTRDeviceController? = null
            try {
                controller = factory.createControllerOnNewFabric(params, error = null)
            } catch(t: Throwable) {
                try {
                    controller = factory.createControllerOnExistingFabric(params, error = null)
                } catch(t: Throwable) {
                // Handle errors.
                }
            }

            val myDelegate = MyControllerDelegate()
            controller!!.setDeviceControllerDelegate(myDelegate, null)

// Create the `MTRSetupPayload` then start the commissioning.
            val payload = MTRSetupPayload(payload = "")

            controller.setupCommissioningSessionWithPayload(payload = payload, newNodeID = nodeID, error = null)
        }
    }
}

//class MyCertificateIssuer : NSObject(), MTROperationalCertificateIssuerProtocol {
//    override fun issueOperationalCertificateForRequest(
//        csrInfo: MTROperationalCSRInfo,
//        attestationInfo: MTRDeviceAttestationInfo,
//        controller: MTRDeviceController,
//        completion: (MTROperationalCertificateChain?, NSError?) -> Unit
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override fun shouldSkipAttestationCertificateValidation(): Boolean {
//        return false
//    }
//
//}

val nodeID = 1

class MyControllerDelegate : NSObject(), MTRDeviceControllerDelegateProtocol {
    override fun controller(controller: MTRDeviceController, statusUpdate: MTRCommissioningStatus) {
        super.controller(controller, statusUpdate)
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
        super.controller(controller, commissioningComplete, nodeID, metrics)
    }

    override fun controller(
        controller: MTRDeviceController,
        commissioneeHasReceivedNetworkCredentials: NSNumber
    ) {
        super.controller(controller, commissioneeHasReceivedNetworkCredentials)
    }
}
