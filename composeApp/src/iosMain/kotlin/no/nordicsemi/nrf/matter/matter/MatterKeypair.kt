@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.matter

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.CFNumberRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFCopyStringDictionaryKeyCallBacks
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Matter.MTRKeypairProtocol
import platform.Security.SecKeyCopyExternalRepresentation
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
import platform.darwin.UInt8Var

sealed class MatterKeypairException : Throwable() {
    object CopyPublicKeyFailed : MatterKeypairException()

    object GeneratePrivateKeyFailed : MatterKeypairException()
    object GeneratePrivateKeyReturnedNil : MatterKeypairException()
}
fun SecKeyRef.asString(): String? = memScoped {
    val error = alloc<CFErrorRefVar>()
    val data = SecKeyCopyExternalRepresentation(this@asString, error.ptr)
    return createNSData(data!!)?.base64EncodedStringWithOptions(0.toULong())
}

@ExperimentalForeignApi
class MatterKeypair : NSObject(), MTRKeypairProtocol {

     val privateKey: SecKeyRef
     val publicKey: SecKeyRef

    init {
        this.privateKey = generatePrivateKey()
        this.publicKey = SecKeyCopyPublicKey(privateKey) ?: throw MatterKeypairException.CopyPublicKeyFailed
    }

    override fun publicKey(): SecKeyRef {
        return publicKey
    }

    override fun signMessageECDSA_DER(message: NSData): NSData = memScoped {
        val error = alloc<CFErrorRefVar>()

        val signature = SecKeyCreateSignature(
            privateKey,
            kSecKeyAlgorithmECDSASignatureMessageX962SHA256,
            createCFData(message),
            error.ptr
        )

        if (signature == null || error.value != null) {
            throw IllegalStateException("Failed to sign message")
        }

        return createNSData(signature)
    }

    private fun generatePrivateKey(): SecKeyRef = memScoped {
        val error = alloc<CFErrorRefVar>()

        val result = SecKeyCreateRandomKey(createAttributes(), error.ptr)

        if (result == null || error.value != null) {
            throw MatterKeypairException.GeneratePrivateKeyReturnedNil
        }

        return result
    }

    private fun createAttributes(): CFDictionaryRef {
        val keys = listOf(
            kSecAttrKeyType,
            kSecAttrKeyClass,
            kSecAttrKeySizeInBits,
            kSecAttrIsPermanent
        )

        val cfSize = createNumber(256)
        val cfIsPermanent = kCFBooleanFalse

        val values = listOf(
            kSecAttrKeyTypeECSECPrimeRandom,
            kSecAttrKeyClassPrivate,
            cfSize,
            cfIsPermanent
        )

        val dictionary = memScoped {
            val keysPtr = allocArrayOf(keys)
            val valuesPtr = allocArrayOf(values)

            CFDictionaryCreate(
                kCFAllocatorDefault,
                keysPtr.reinterpret(),
                valuesPtr.reinterpret(),
                keys.size.toLong(),
                kCFCopyStringDictionaryKeyCallBacks.ptr,
                kCFTypeDictionaryValueCallBacks.ptr
            )
        }

        keys.forEach { CFRelease(it) }
        values.forEach { CFRelease(it) }

        return dictionary!!
    }

    private fun createNumber(value: Int): CFNumberRef? = memScoped {
        val intValue = alloc<IntVar>()
        intValue.value = value

        CFNumberCreate(
            null,
            kCFNumberIntType,
            intValue.ptr
        )
    }


}

private fun createCFData(data: NSData): CFDataRef? {
    val length = data.length.toLong()
    val bytes = data.bytes?.reinterpret<UInt8Var>() ?: return null

    return CFDataCreate(kCFAllocatorDefault, bytes, length)
}

@OptIn(BetaInteropApi::class)
private fun createNSData(data: CFDataRef): NSData {
    val bytes = CFDataGetBytePtr(data)
    val opaque: COpaquePointer? = bytes?.reinterpret()
    val length = CFDataGetLength(data)

    return NSData.create(bytes = opaque, length = length.toULong())
}
