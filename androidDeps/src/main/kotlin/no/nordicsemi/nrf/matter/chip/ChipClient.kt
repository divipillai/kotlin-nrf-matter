package no.nordicsemi.nrf.matter.chip

import android.content.Context
import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipDeviceController
import chip.devicecontroller.ChipStructs
import chip.devicecontroller.CommissionParameters
import chip.devicecontroller.ControllerParams
import chip.devicecontroller.GetConnectedDeviceCallbackJni
import chip.devicecontroller.InvokeCallback
import chip.devicecontroller.ReportCallback
import chip.devicecontroller.SubscriptionEstablishedCallback
import chip.devicecontroller.model.AttributeState
import chip.devicecontroller.model.ChipAttributePath
import chip.devicecontroller.model.ChipEventPath
import chip.devicecontroller.model.InvokeElement
import chip.devicecontroller.model.NodeState
import chip.platform.AndroidBleManager
import chip.platform.AndroidChipLogging
import chip.platform.AndroidChipPlatform
import chip.platform.AndroidNfcCommissioningManager
import chip.platform.ChipMdnsCallbackImpl
import chip.platform.DiagnosticDataProviderImpl
import chip.platform.NsdManagerServiceBrowser
import chip.platform.NsdManagerServiceResolver
import chip.platform.PreferencesConfigurationManager
import chip.platform.PreferencesKeyValueStoreManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import matter.tlv.AnonymousTag
import matter.tlv.ContextSpecificTag
import matter.tlv.TlvWriter
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.DeviceId
import java.util.Optional
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

/**
 * Owns the lifecycle of the CHIP (Matter) native device controller and exposes coroutine-based
 * wrappers around its callback-based APIs: commissioning, pairing, attribute reads/writes,
 * command invocation, attribute subscription, and fabric management for decommissioning.
 *
 * @property context Android context used to initialize the underlying Android CHIP platform
 *   (BLE, NFC, mDNS, and persistent storage integrations).
 */
class ChipClient(
    private val context: Context,
) {
    /**
     * Stream of native CHIP SDK log lines, each prefixed with its originating module name.
     * Buffers up to 200 entries, dropping the oldest on overflow.
     */
    val chipLogFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 200,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * The lazily-initialized [ChipDeviceController] backing all operations in this class.
     *
     * Initialization loads the native CHIP JNI library, wires native log output into
     * [chipLogFlow], configures the Android CHIP platform (BLE, NFC, mDNS, and
     * preference-backed storage), and constructs the controller with [VENDOR_ID] as its
     * vendor ID.
     */
    val chipDeviceController: ChipDeviceController by lazy {
        ChipDeviceController.loadJni()

        AndroidChipLogging.setLogCallback { module, _, message ->

            chipLogFlow.tryEmit("[$module] $message")
        }

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
     * Resolves a native device pointer for an already-commissioned node, establishing a CASE
     * session with it if one is not already active.
     *
     * @param nodeId Matter node ID of the device to connect to.
     * @return native pointer to the connected device, valid until released via
     *   [ChipDeviceController.releaseConnectedDevicePointer].
     * @throws IllegalStateException if the connection attempt fails.
     */
    suspend fun getConnectedDevicePointer(nodeId: Long): Long {
        return suspendCancellableCoroutine { continuation ->
            chipDeviceController.getConnectedDevicePointer(
                nodeId,
                object : GetConnectedDeviceCallbackJni.GetConnectedDeviceCallback {
                    override fun onDeviceConnected(devicePointer: Long) {
                        continuation.resume(devicePointer)
                    }

                    override fun onConnectionFailure(nodeId: Long, error: Exception) {
                        val errorMessage = "Unable to get connected device with nodeId $nodeId."
                        NordicLogger.error(errorMessage, error, tag = TAG)
                        continuation.resumeWithException(IllegalStateException(errorMessage))
                    }
                })
        }
    }


    /**
     * Removes this app's fabric — and any other fabric present on the device — from a
     * commissioned device, returning it to a factory-uncommissioned state from the device's
     * perspective.
     *
     * Reads all fabrics on the device, removes every fabric other than this app's own first
     * (for example, a foreign fabric left behind by commissioning through another ecosystem
     * such as Google Home), then removes this app's own fabric last. Errors removing
     * individual fabrics are logged and do not abort the operation; the connected device
     * pointer is always released before returning.
     *
     * @param deviceId Matter node ID of the device to decommission.
     */
    suspend fun decommissionDevice(deviceId: Long) {
        NordicLogger.info("Decommission device: $deviceId", tag = TAG)

        var connectedDevicePtr: Long? = null

        try {
            connectedDevicePtr = getConnectedDevicePointer(deviceId)
            // Read ALL fabrics (fabric-filtered = false)
            val fabrics = readFabrics(connectedDevicePtr, fabricFiltered = false)
            val ownFabrics = readFabrics(connectedDevicePtr, fabricFiltered = true)

            if (fabrics.isEmpty()) {
                NordicLogger.info("No fabrics — already decommissioned", tag = TAG)
                return
            }

            // Filter out our own fabric from the list of fabrics to remove.
            val foreignFabrics = fabrics.filterNot { fabric ->
                ownFabrics.any { it.fabricIndex == fabric.fabricIndex }
            }

            // Since we commissioned the device using the Google Home app,
            // it will have a foreign fabric that we need to remove first.
            // Then we can remove our own fabric last.
            for (fabric in foreignFabrics) {
                runCatching {
                    removeFabric(connectedDevicePtr, fabric.fabricIndex)
                }.onSuccess {
                    NordicLogger.info(
                        "Foreign fabric ${fabric.fabricIndex} removed",
                        tag = TAG
                    )
                }.onFailure {
                    NordicLogger.error(
                        "Error removing foreign fabric ${fabric.fabricIndex}: $it",
                        it as? Exception,
                        tag = TAG
                    )
                }
            }

            // Remove own fabric
            ownFabrics.firstOrNull()?.let { fabric ->
                NordicLogger.info("Removing own fabric index=${fabric.fabricIndex}... ", TAG)
                runCatching {
                    removeFabric(connectedDevicePtr, fabric.fabricIndex)
                }.onFailure {
                    NordicLogger.error(
                        "Failed removing own fabric ${fabric.fabricIndex}",
                        it as? Exception,
                        tag = TAG
                    )
                }
            }
            NordicLogger.info("Device $deviceId fully decommissioned.", tag = TAG)
        } finally {
            connectedDevicePtr?.let {
                chipDeviceController.releaseConnectedDevicePointer(it)
            }
            NordicLogger.info("Released connected device pointer for device $deviceId", tag = TAG)
        }
    }

    /**
     * Reads the Operational Credentials cluster's `Fabrics` attribute on endpoint 0.
     *
     * @param connectedDevicePtr native pointer to the connected device.
     * @param fabricFiltered if `true`, restricts the result to the fabric of the caller's
     *   active session; if `false`, returns all fabrics known to the device.
     * @return the fabric descriptors reported by the device.
     * @throws Exception if the underlying read fails.
     */
    private suspend fun readFabrics(
        connectedDevicePtr: Long,
        fabricFiltered: Boolean
    ): List<ChipStructs.OperationalCredentialsClusterFabricDescriptorStruct> =
        suspendCancellableCoroutine { continuation ->
            val cluster =
                ChipClusters.OperationalCredentialsCluster(connectedDevicePtr, 0)

            val callback =
                object : ChipClusters.OperationalCredentialsCluster.FabricsAttributeCallback {
                    override fun onSuccess(
                        values: List<ChipStructs.OperationalCredentialsClusterFabricDescriptorStruct>
                    ) {
                        continuation.resume(values)
                    }

                    override fun onError(error: Exception) {
                        continuation.resumeWithException(error)
                    }
                }

            cluster.readFabricsAttributeWithFabricFilter(callback, fabricFiltered)
        }

    /**
     * Removes the fabric at [fabricIndex] from the device via the Operational Credentials
     * cluster's `RemoveFabric` command.
     *
     * @param connectedDevicePtr native pointer to the connected device.
     * @param fabricIndex index of the fabric to remove, as reported by [readFabrics].
     * @throws Exception if the command fails.
     */
    private suspend fun removeFabric(
        connectedDevicePtr: Long,
        fabricIndex: Int
    ) = suspendCancellableCoroutine { continuation ->
        ChipClusters.OperationalCredentialsCluster(connectedDevicePtr, 0)
            .removeFabric(
                object : ChipClusters.OperationalCredentialsCluster.NOCResponseCallback {
                    override fun onSuccess(
                        statusCode: Int?,
                        fabricIndex: Optional<Int?>?,
                        debugText: Optional<String?>?
                    ) {
                        continuation.resume(Unit)
                    }

                    override fun onError(error: Exception) {
                        continuation.resumeWithException(error)
                    }
                },
                fabricIndex
            )
    }

    /**
     * Establishes a PASE (Password-Authenticated Session Establishment) connection with a
     * device over IP, as the first step of commissioning.
     *
     * Suspends until the connection is established, commissioning info has been read, a
     * commissioning status update is received, or pairing completes — whichever the native
     * SDK reports first for this flow.
     *
     * @param deviceId node ID to assign to the device being paired.
     * @param ipAddress IP address of the device to connect to.
     * @param port port of the device to connect to.
     * @param setupPinCode setup PIN code from the device's onboarding payload.
     * @throws IllegalStateException if pairing completes with a non-zero error code.
     * @throws Throwable if the native SDK reports an error during the connection attempt.
     */
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


    /**
     * Commissions a device that already has an established PASE connection, completing the
     * Matter commissioning flow with no network credentials supplied (for devices that do not
     * require Wi-Fi/Thread network provisioning during commissioning).
     *
     * @param deviceId node ID of the device to commission, as previously used with
     *   [awaitEstablishPaseConnection].
     * @throws IllegalStateException if commissioning completes with a non-zero error code.
     * @throws Throwable if the native SDK reports an error during commissioning.
     */
    suspend fun awaitCommissionDevice(deviceId: DeviceId) {
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
                .setNetworkCredentials(null)
                .build()

            chipDeviceController.commissionDevice(deviceId.longValue, commissionParameters)
        }
    }

    /**
     * Reads a single attribute.
     *
     * Convenience wrapper over [readAttributes] for callers that need only one attribute.
     *
     * @param devicePtr native pointer to the connected device.
     * @param attributePath path of the attribute to read.
     * @return the attribute's state, or `null` if the device did not report it.
     * @throws IllegalStateException if the underlying read fails.
     */
    suspend fun readAttribute(devicePtr: Long, attributePath: ChipAttributePath): AttributeState? {
        return readAttributes(devicePtr, listOf(attributePath))[attributePath]
    }

    /**
     * Reads one or more attributes from a device in a single interaction.
     *
     * @param devicePtr native pointer to the connected device.
     * @param attributePaths paths of the attributes to read.
     * @return a map from each requested path to its resolved [AttributeState]; paths the
     *   device did not report are omitted from the map. The read is subject to a 30-second
     *   timeout.
     * @throws IllegalStateException if the underlying read fails.
     */
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
                        NordicLogger.error("Error on readAttributes Callback!", e, tag = TAG)
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
                NordicLogger.debug("Read attribute coroutine cancelled", tag = TAG)
            }
        }
    }

    /**
     * Invokes a command with a hardcoded single-field TLV payload (an unsigned byte with
     * value `2` in context-specific tag 0).
     *
     * Resolves a connected device pointer for [deviceId] before invoking. Errors from the
     * invocation are logged and swallowed rather than propagated.
     *
     * @param deviceId node ID of the device to invoke the command on.
     * @param endpoint endpoint hosting the target cluster.
     * @param clusterId cluster ID of the command to invoke.
     * @param commandId command ID to invoke.
     */
    suspend fun setLet(
        deviceId: DeviceId,
        endpoint: Int,
        clusterId: Long,
        commandId: Long,
    ) {
        val ptr = getConnectedDevicePointer(deviceId.longValue)
        return suspendCancellableCoroutine { continuation ->

            val tlvWriter = TlvWriter()
            tlvWriter.startStructure(AnonymousTag)
            tlvWriter.put(ContextSpecificTag(0), 2.toUByte())
            tlvWriter.endStructure()
            val invokeElement =
                InvokeElement.newInstance(
                    endpoint,
                    clusterId,
                    commandId,
                    tlvWriter.getEncoded(),
                    null
                )

            val customInvokeCallback = object : InvokeCallback {

                override fun onError(e: Exception) {
                    NordicLogger.error("Error on invoke Callback!", e, tag = TAG)
                    continuation.resume(Unit)
                }

                override fun onResponse(
                    invokeElement: InvokeElement?,
                    successCode: Long
                ) {
                    NordicLogger.info(
                        "Command Response Success!",
                        tag = "SetLet"
                    )
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

    /**
     * Invokes a command with a hardcoded single-field TLV payload (a boolean `true` in
     * context-specific tag 0).
     *
     * @param devicePtr native pointer to the connected device.
     * @param path endpoint and cluster of the command to invoke; its attribute ID is used as
     *   the command ID.
     * @throws Exception if the invocation fails.
     */
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
                    NordicLogger.error("Error on invoke Callback!", e, tag = TAG)
                    continuation.resumeWithException(e)
                }

                override fun onResponse(
                    invokeElement: InvokeElement?,
                    successCode: Long
                ) {
                    NordicLogger.info(
                        "Command Response Success!",
                        tag = "GenerateRandomNumber"
                    )
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

    /**
     * Invokes a cluster command on a device.
     *
     * @param devicePtr native pointer to the connected device.
     * @param invokeElement command to invoke, encoding its endpoint, cluster, command ID, and
     *   TLV-encoded fields.
     * @param timedRequestTimeoutMs timeout, in milliseconds, for the timed-invoke window;
     *   defaults to [DEFAULT_TIMEOUT].
     * @param imTimeoutMs timeout, in milliseconds, for the Interaction Model exchange;
     *   defaults to [DEFAULT_TIMEOUT].
     * @return the success status code returned by the device.
     * @throws IllegalStateException if the invocation fails.
     */
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

    /**
     * Subscribes to one or more attributes, delivering ongoing reports to [reportCallback].
     *
     * @param reportCallback callback invoked with each attribute report and on error.
     * @param devicePtr native pointer to the connected device.
     * @param attributePaths paths of the attributes to subscribe to.
     * @param minIntervalS minimum reporting interval, in seconds.
     * @param maxIntervalS maximum reporting interval, in seconds.
     * @param timeoutMs timeout, in milliseconds, for establishing the subscription.
     */
    fun subscribeAttribute(
        reportCallback: ReportCallback,
        devicePtr: Long,
        attributePaths: List<ChipAttributePath>,
        minIntervalS: Int,
        maxIntervalS: Int,
        timeoutMs: Int,
    ) {
        chipDeviceController.subscribeToAttributePath(
            object : SubscriptionEstablishedCallback {
                override fun onSubscriptionEstablished(subscriptionId: Long) {
                    NordicLogger.debug(
                        "Subscription established: $subscriptionId",
                        tag = "SubscribeAttribute"
                    )
                }
            },
            reportCallback,
            devicePtr,
            attributePaths,
            minIntervalS,
            maxIntervalS,
            timeoutMs,
        )
    }

    companion object {
        private val TAG: String
            get() = ChipClient::class.java.simpleName
    }

}
