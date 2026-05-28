package no.nordicsemi.nrf.matter.chip

import android.content.Context
import chip.devicecontroller.ChipDeviceController
import chip.devicecontroller.CommissionParameters
import chip.devicecontroller.ControllerParams
import chip.devicecontroller.GetConnectedDeviceCallbackJni
import chip.devicecontroller.InvokeCallback
import chip.devicecontroller.NetworkCredentials
import chip.devicecontroller.ReportCallback
import chip.devicecontroller.SubscriptionEstablishedCallback
import chip.devicecontroller.UnpairDeviceCallback
import chip.devicecontroller.model.AttributeState
import chip.devicecontroller.model.ChipAttributePath
import chip.devicecontroller.model.ChipEventPath
import chip.devicecontroller.model.InvokeElement
import chip.devicecontroller.model.NodeState
import chip.platform.AndroidBleManager
import chip.platform.AndroidChipPlatform
import chip.platform.AndroidNfcCommissioningManager
import chip.platform.ChipMdnsCallbackImpl
import chip.platform.DiagnosticDataProviderImpl
import chip.platform.NsdManagerServiceBrowser
import chip.platform.NsdManagerServiceResolver
import chip.platform.PreferencesConfigurationManager
import chip.platform.PreferencesKeyValueStoreManager
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import matter.tlv.AnonymousTag
import matter.tlv.ContextSpecificTag
import matter.tlv.TlvWriter
import no.nordicsemi.nrf.matter.model.DeviceId
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/*
 * Copyright (c) 2025, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/* 0xFFF4 is a test vendor ID, replace with your assigned company ID */
private const val VENDOR_ID = 0xFFF4

private const val DEFAULT_TIMEOUT = 1000

class ChipClient(
    private val context: Context,
) {
    // Lazily instantiate [ChipDeviceController] and hold a reference to it.
    val chipDeviceController: ChipDeviceController by lazy {
        ChipDeviceController.loadJni()
        AndroidChipPlatform(
            AndroidBleManager(),
            AndroidNfcCommissioningManager(),
            PreferencesKeyValueStoreManager(context),
            PreferencesConfigurationManager(context),
            NsdManagerServiceResolver(context),
            NsdManagerServiceBrowser(context),
            ChipMdnsCallbackImpl(),
            DiagnosticDataProviderImpl(context)
        )
        ChipDeviceController(
            ControllerParams.newBuilder().setUdpListenPort(0).setControllerVendorId(VENDOR_ID)
                .build()
        )
    }

    /**
     * Wrapper around [ChipDeviceController.getConnectedDevicePointer] to return the value directly.
     */
    suspend fun getConnectedDevicePointer(nodeId: Long): Long {
        return suspendCancellableCoroutine { continuation ->
            chipDeviceController.getConnectedDevicePointer(
                nodeId,
                object : GetConnectedDeviceCallbackJni.GetConnectedDeviceCallback {
                    override fun onDeviceConnected(devicePointer: Long) {
                        Napier.i { "Got connected device pointer" }
                        continuation.resume(devicePointer)
                    }

                    override fun onConnectionFailure(nodeId: Long, error: Exception) {
                        val errorMessage = "Unable to get connected device with nodeId $nodeId."
                        Napier.e(error) { errorMessage }
                        continuation.resumeWithException(IllegalStateException(errorMessage))
                    }
                })
        }
    }

    /**
     * Removes the app's fabric from the device.
     *
     * @param nodeId node identifier
     */
    suspend fun awaitUnpairDevice(nodeId: Long) {
        return suspendCancellableCoroutine { continuation ->
            val callback: UnpairDeviceCallback =
                object : UnpairDeviceCallback {
                    override fun onError(status: Int, nodeId: Long) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                IllegalStateException(
                                    "Failed unpairing device [$nodeId] with status [$status]"
                                )
                            )
                        }
                    }

                    override fun onSuccess(nodeId: Long) {
                        if (continuation.isActive) {
                            Napier.i { "awaitUnpairDevice.onSuccess: deviceId [$nodeId]" }
                            continuation.resume(Unit)
                        }
                    }
                }
            chipDeviceController.unpairDeviceCallback(nodeId, callback)
            continuation.invokeOnCancellation {
                Napier.i { "Unpair coroutine cancelled" }
            }
        }
    }

    suspend fun awaitEstablishPaseConnection(
        deviceId: DeviceId,
        ipAddress: String,
        port: Int,
        setupPinCode: Long
    ) {
        return suspendCancellableCoroutine { continuation ->
            chipDeviceController.setCompletionListener(
                object : BaseCompletionListener() {
                    override fun onConnectDeviceComplete() {
                        super.onConnectDeviceComplete()
                        continuation.resume(Unit)
                    }

                    // Note that an error in processing is not necessarily communicated via onError().
                    // onCommissioningComplete with a "code != 0" also denotes an error in processing.
                    override fun onPairingComplete(errorCode: Long) {
                        super.onPairingComplete(errorCode)
                        if (errorCode != 0L) {
                            continuation.resumeWithException(
                                IllegalStateException("Pairing failed with error code [${errorCode}]")
                            )
                        } else {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(error: Throwable) {
                        super.onError(error)
                        continuation.resumeWithException(error)
                    }

                    override fun onReadCommissioningInfo(
                        vendorId: Int,
                        productId: Int,
                        wifiEndpointId: Int,
                        threadEndpointId: Int
                    ) {
                        super.onReadCommissioningInfo(
                            vendorId,
                            productId,
                            wifiEndpointId,
                            threadEndpointId
                        )
                        continuation.resume(Unit)
                    }

                    override fun onCommissioningStatusUpdate(
                        nodeId: Long,
                        stage: String?,
                        errorCode: Long
                    ) {
                        super.onCommissioningStatusUpdate(nodeId, stage, errorCode)
                        continuation.resume(Unit)
                    }
                })
            chipDeviceController.establishPaseConnection(
                deviceId.longValue,
                ipAddress,
                port,
                setupPinCode
            )
        }
    }

    suspend fun awaitCommissionDevice(deviceId: DeviceId, networkCredentials: NetworkCredentials?) {
        return suspendCancellableCoroutine { continuation ->
            chipDeviceController.setCompletionListener(
                object : BaseCompletionListener() {
                    override fun onCommissioningComplete(nodeId: Long, errorCode: Long) {
                        super.onCommissioningComplete(nodeId, errorCode)
                        if (errorCode != 0L) {
                            continuation.resumeWithException(
                                IllegalStateException("Commissioning failed with error code [${errorCode}]")
                            )
                        } else {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(error: Throwable) {
                        super.onError(error)
                        continuation.resumeWithException(error)
                    }
                })

            val commissionParameters = CommissionParameters.Builder()
                .setNetworkCredentials(networkCredentials)
                .build()

            chipDeviceController.commissionDevice(deviceId.longValue, commissionParameters)
        }
    }

    suspend fun readAttribute(devicePtr: Long, attributePath: ChipAttributePath): AttributeState? {
        return readAttributes(devicePtr, listOf(attributePath))[attributePath]
    }

    /** Wrapper around [ChipDeviceController.readAttributePath] */
    suspend fun readAttributes(
        devicePtr: Long,
        attributePaths: List<ChipAttributePath>
    ): Map<ChipAttributePath, AttributeState> {
        return suspendCancellableCoroutine { continuation ->
            val callback: ReportCallback =
                object : ReportCallback {
                    override fun onError(
                        attributePath: ChipAttributePath?,
                        eventPath: ChipEventPath?,
                        e: Exception
                    ) {
                        Napier.i("Oh no!")
                        continuation.resumeWithException(
                            IllegalStateException(
                                "readAttributes failed",
                                e
                            )
                        )
                    }

                    override fun onReport(nodeState: NodeState?) {
                        val states: HashMap<ChipAttributePath, AttributeState> = HashMap()
                        for (path in attributePaths) {
                            val endpoint: Int = path.endpointId.id.toInt()
                            nodeState?.getEndpointState(endpoint)
                                ?.getClusterState(path.clusterId.id)
                                ?.getAttributeState(path.attributeId.id)?.let {
                                    states[path] = it
                                }
                        }
                        continuation.resume(states)
                    }

                }

            chipDeviceController.readAttributePath(callback, devicePtr, attributePaths, 30_000)

            continuation.invokeOnCancellation {
                // Optional: abort the interaction if the coroutine is canceled
                // chipDeviceController.shutdownSubscriptions() or similar, if available
                Napier.d { "AAA, read attribute coroutine cancelled" }
            }
        }
    }

    suspend fun setLet(
        deviceId: DeviceId
    ) {
        val ptr = getConnectedDevicePointer(deviceId.longValue)
        return suspendCancellableCoroutine { continuation ->

            val tlvWriter = TlvWriter()
            tlvWriter.startStructure(AnonymousTag)
            tlvWriter.put(ContextSpecificTag(0), 2.toUByte())
            tlvWriter.endStructure()
            val invokeElement =
                InvokeElement.newInstance(
                    1,
                    0xFFF1FC01,
                    0xFFF10000,
                    tlvWriter.getEncoded(),
                    null
                )

            val customInvokeCallback = object : InvokeCallback {

                override fun onError(e: Exception) {
                    Napier.i("Error on invoke Callback!, ${e.printStackTrace()}")
                    continuation.resume(Unit)
                }

                override fun onResponse(
                    invokeElement: InvokeElement?,
                    successCode: Long
                ) {
                    Napier.i("Command successs: ${invokeElement}")
                    Napier.i("Command successs tlv: ${invokeElement?.tlvByteArray}")
                    Napier.i("Command successs json: ${invokeElement?.jsonString}")
                    continuation.resume(Unit)
                }

            }

            chipDeviceController.invoke(
                customInvokeCallback,
                ptr,
                invokeElement,
                15_000,
                30_000,
            )
        }
    }

    suspend fun generateRandomNumber(
        devicePtr: Long,
        path: ChipAttributePath
    ) {
        return suspendCancellableCoroutine { continuation ->
            val fields = TlvWriter().apply {
                startStructure(AnonymousTag)
                put(
                    ContextSpecificTag(0),
                    true
                ) // replace with appropriate put() overload for your type
                endStructure()
            }.getEncoded()


            val customInvokeCallback = object : InvokeCallback {

                override fun onError(e: Exception) {
                    Napier.i("Error on invoke Callback!, ${e.printStackTrace()}")
                    continuation.resumeWithException(e)
                }

                override fun onResponse(
                    invokeElement: InvokeElement?,
                    successCode: Long
                ) {
                    Napier.i("Command successs: ${invokeElement}")
                    Napier.i("Command successs tlv: ${invokeElement?.tlvByteArray}")
                    Napier.i("Command successs json: ${invokeElement?.jsonString}")
                    continuation.resume(Unit)
                }

            }

            val invokeElement = InvokeElement.newInstance(
                path.endpointId,
                path.clusterId,
                path.attributeId,
                fields,
                null
            )

            chipDeviceController.invoke(
                customInvokeCallback,
                devicePtr,
                invokeElement,
                15_000,
                30_000,
            )
        }
    }

    /** Wrapper around [ChipDeviceController.subscribeToAttributePath] */
    fun subscribeToAttribute(
        deviceId: DeviceId,
        endpoint: Int,
        clusterId: Long,
        attributeId: Long,
    ): Flow<Boolean> = callbackFlow {
        val devicePtr = getConnectedDevicePointer(deviceId.longValue)
        val attributePath = ChipAttributePath.newInstance(
            endpoint,
            clusterId,
            attributeId
        )

        chipDeviceController.subscribeToAttributePath(
            object : SubscriptionEstablishedCallback {
                override fun onSubscriptionEstablished(subscriptionId: Long) {
                    Napier.d(
                        "Subscription established: $subscriptionId",
                        tag = "SubscribeToAttribute"
                    )
                }
            },

            object : ReportCallback {

                override fun onError(
                    onError: ChipAttributePath?,
                    eventPath: ChipEventPath?,
                    e: Exception
                ) {
                    Napier.e(
                        "Subscription error", e,
                        tag = "SubscribeToAttribute"
                    )

                    close(e)
                }

                override fun onReport(nodeState: NodeState?) {

                    try {
                        val endpointState =
                            nodeState
                                ?.endpointStates
                                ?.get(endpoint)

                        val clusterState =
                            endpointState
                                ?.getClusterState(clusterId)


                        val attributeState =
                            clusterState
                                ?.getAttributeState(attributeId)


                        val value = attributeState?.value as? Boolean

                        Napier.d("Button pressed: $value", tag = "SubscribeToAttribute")

                        if (value != null) {
                            trySend(value)
                        }

                    } catch (e: Exception) {
                        close(e)
                    }
                }
            },

            devicePtr,
            listOf(attributePath),
            0,
            30,
            30_000
        )

        awaitClose {
            Napier.d("Subscription closed", tag = "SubscribeToAttribute")
            chipDeviceController.shutdownSubscriptions()
        }
    }

    /** Wrapper around [ChipDeviceController.invoke] */
    suspend fun invoke(
        devicePtr: Long,
        invokeElement: InvokeElement,
        timedRequestTimeoutMs: Int = DEFAULT_TIMEOUT,
        imTimeoutMs: Int = DEFAULT_TIMEOUT
    ): Long {
        return suspendCancellableCoroutine { continuation ->
            val invokeCallback: InvokeCallback =
                object : InvokeCallback {
                    override fun onError(e: java.lang.Exception?) {
                        continuation.resumeWithException(IllegalStateException("invoke failed", e))
                    }

                    override fun onResponse(invokeElement: InvokeElement?, successCode: Long) {
                        continuation.resume(successCode)
                    }
                }
            chipDeviceController.invoke(
                invokeCallback, devicePtr, invokeElement, timedRequestTimeoutMs, imTimeoutMs
            )
        }
    }

}
