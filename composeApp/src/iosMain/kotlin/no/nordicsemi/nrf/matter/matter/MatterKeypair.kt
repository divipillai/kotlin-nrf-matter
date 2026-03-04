@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.matter

import io.github.aakira.napier.Napier
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
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFCopyStringDictionaryKeyCallBacks
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Matter.MTRKeypairProtocol
import platform.Security.SecItemCopyMatching
import platform.Security.SecKeyCopyExternalRepresentation
import platform.Security.SecKeyCopyPublicKey
import platform.Security.SecKeyCreateRandomKey
import platform.Security.SecKeyCreateSignature
import platform.Security.SecKeyRef
import platform.Security.errSecSuccess
import platform.Security.kSecAttrApplicationTag
import platform.Security.kSecAttrIsPermanent
import platform.Security.kSecAttrKeyClass
import platform.Security.kSecAttrKeyClassPrivate
import platform.Security.kSecAttrKeySizeInBits
import platform.Security.kSecAttrKeyType
import platform.Security.kSecAttrKeyTypeECSECPrimeRandom
import platform.Security.kSecClass
import platform.Security.kSecClassKey
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

        val existingKey = createOrGetCKey()

        if (existingKey == null) {
            Napier.i("Haven't found an existing key. Creating new.")
        } else {
            Napier.i("Existing key found.")
            return existingKey
        }

        val result = SecKeyCreateRandomKey(createAttributes(), error.ptr)

        if (result == null || error.value != null) {
            Napier.i("Couldn't create a new key.")
            throw MatterKeypairException.GeneratePrivateKeyReturnedNil
        }

        Napier.i("Successfully create new key.")

        return result
    }

    fun createOrGetCKey(): SecKeyRef? = memScoped {
        val result = alloc<CFTypeRefVar>()

        val status = SecItemCopyMatching(createAttributes(), result.ptr)

        Napier.i("Matching key status: $status.")

        if (status != errSecSuccess) {
            return null
        }

        return result.value as SecKeyRef?
    }

    private fun createAttributes(): CFDictionaryRef {
        val tag = createCFData("nordicsemi.nrf.matter".nsdata()!!)

        val keys = listOf(
            kSecAttrApplicationTag,
            kSecAttrKeyType,
            kSecAttrKeyClass,
            kSecAttrKeySizeInBits,
            kSecAttrIsPermanent
        )

        val cfSize = createNumber(256)
        val cfIsPermanent = kCFBooleanFalse

        val values = listOf(
            tag,
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

fun String.nsdata(): NSData? =
    NSString.create(string = this).dataUsingEncoding(NSUTF8StringEncoding)

fun NSData.string(): String? =
    NSString.create(data = this, encoding = NSUTF8StringEncoding)?.toString()
