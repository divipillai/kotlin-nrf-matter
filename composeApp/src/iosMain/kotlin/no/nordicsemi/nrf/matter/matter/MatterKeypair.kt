package no.nordicsemi.nrf.matter.matter

import cnames.structs.__CFString
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFErrorRefVar
import platform.Foundation.NSData
import platform.Matter.MTRKeypairProtocol
import platform.Security.SecKeyCopyPublicKey
import platform.Security.SecKeyCreateRandomKey
import platform.Security.SecKeyCreateSignature
import platform.Security.SecKeyRef
import platform.Security.kSecAttrIsPermanent
import platform.Security.kSecAttrKeyClass
import platform.Security.kSecAttrKeyClassPrivate
import platform.Security.kSecAttrKeySizeInBits
import platform.Security.kSecAttrKeyType
import platform.Security.kSecAttrKeyTypeECSECPrimeRandom
import platform.Security.kSecKeyAlgorithmECDSASignatureMessageX962SHA256
import platform.darwin.NSObject

sealed class MatterKeypairException : Throwable() {
    object CopyPublicKeyFailed : MatterKeypairException()
    object GeneratePrivateKeyFailed : MatterKeypairException()
    object GeneratePrivateKeyReturnedNil : MatterKeypairException()
}

@ExperimentalForeignApi
class MatterKeypair : NSObject(), MTRKeypairProtocol {

    private val privateKey: SecKeyRef
    private val publicKey: SecKeyRef

    init {
        this.privateKey = generatePrivateKey()
        this.publicKey = SecKeyCopyPublicKey(privateKey) ?: throw MatterKeypairException.CopyPublicKeyFailed
    }

    override fun publicKey(): SecKeyRef {
        return publicKey
    }

    override fun signMessageECDSA_DER(message: NSData): NSData {
        var error: CValuesRef<CFErrorRefVar>? = null // TODO

        val signature = SecKeyCreateSignature(
            privateKey,
            kSecKeyAlgorithmECDSASignatureMessageX962SHA256,
            message as CFDataRef,
            error
        ) as NSData?

        if (signature == null) {
            throw IllegalStateException("Failed to sign message")
        }
        return signature
    }

    private fun generatePrivateKey(): SecKeyRef {
        val attributes: Map<CPointer<__CFString>?, Any?> = mapOf(
            kSecAttrKeyType to kSecAttrKeyTypeECSECPrimeRandom,
            kSecAttrKeyClass to kSecAttrKeyClassPrivate,
            kSecAttrKeySizeInBits to 256,
            kSecAttrIsPermanent to false
        )

        return SecKeyCreateRandomKey(attributes as CFDictionaryRef, null) //TODO error
            ?: throw MatterKeypairException.GeneratePrivateKeyReturnedNil
    }
}
