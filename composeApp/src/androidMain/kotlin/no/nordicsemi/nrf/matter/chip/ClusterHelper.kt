package no.nordicsemi.nrf.matter.chip

import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipStructs
import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.model.DeviceMatterInfo
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

class ClustersHelper(private val chipClient: ChipClient) {

    // -----------------------------------------------------------------------------------------------
    // Convenience functions

    /** Fetches MatterDeviceInfo for each endpoint supported by the device. */
    suspend fun fetchDeviceMatterInfo(nodeId: Long): List<DeviceMatterInfo> {
        Napier.d { "AAA, fetchDeviceMatterInfo(): nodeId [${nodeId}]" }
        val matterDeviceInfoList = arrayListOf<DeviceMatterInfo>()
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(nodeId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return emptyList()
            }
        fetchDeviceMatterInfo(nodeId, connectedDevicePtr, 0, matterDeviceInfoList)
        return matterDeviceInfoList
    }

    /** Fetches MatterDeviceInfo for a specific endpoint. */
    suspend fun fetchDeviceMatterInfo(
        nodeId: Long,
        connectedDevicePtr: Long,
        endpointInt: Int,
        matterDeviceInfoList: ArrayList<DeviceMatterInfo>
    ) {
        Napier.d { "AAA, fetchDeviceMatterInfo(): nodeId [${nodeId}] endpoint [$endpointInt]" }

        val partsListAttribute =
            readDescriptorClusterPartsListAttribute(connectedDevicePtr, endpointInt)
        Napier.d { "AAA, partsListAttribute [${partsListAttribute}]" }

        // DeviceListAttribute
        val deviceListAttribute =
            readDescriptorClusterDeviceListAttribute(connectedDevicePtr, endpointInt)
        val types = arrayListOf<Long>()
        // todo: device type is deprecated
        deviceListAttribute.forEach { types.add(it.deviceType) }

        // ServerListAttribute
        val serverListAttribute =
            readDescriptorClusterServerListAttribute(connectedDevicePtr, endpointInt)
        val serverClusters = arrayListOf<Long>()
        serverListAttribute.forEach { serverClusters.add(it) }

        // ClientListAttribute
        val clientListAttribute =
            readDescriptorClusterClientListAttribute(connectedDevicePtr, endpointInt)
        val clientClusters = arrayListOf<Long>()
        clientListAttribute.forEach { clientClusters.add(it) }

        // Build the DeviceMatterInfo
        val deviceMatterInfo = DeviceMatterInfo(endpointInt, types, serverClusters, clientClusters)
        matterDeviceInfoList.add(deviceMatterInfo)

        // Recursive call for the parts supported by the endpoint.
        // For each part (endpoint)
        partsListAttribute?.forEach { part ->
            Napier.d { "AAA, part [$part] is [${part.javaClass}]" }
            val endpointInt =
                when (part) {
                    is Int -> part
                    else -> return@forEach
                }
            Napier.d { "AAA, Processing part [$part]" }
            fetchDeviceMatterInfo(nodeId, connectedDevicePtr, endpointInt, matterDeviceInfoList)
        }
    }

    // -----------------------------------------------------------------------------------------------
    // DescriptorCluster functions

    /**
     * PartsListAttribute. These are the endpoints supported.
     *
     * ```
     * For example, on endpoint 0:
     *     sendReadPartsListAttribute part: [1]
     *     sendReadPartsListAttribute part: [2]
     * ```
     */
    suspend fun readDescriptorClusterPartsListAttribute(
        devicePtr: Long,
        endpoint: Int
    ): List<Any>? {
        return suspendCancellableCoroutine { continuation ->
            getDescriptorClusterForDevice(devicePtr, endpoint)
                .readPartsListAttribute(
                    object : ChipClusters.DescriptorCluster.PartsListAttributeCallback {
                        override fun onSuccess(values: MutableList<Int>?) {
                            continuation.resume(values)
                        }

                        override fun onError(ex: Exception) {
                            continuation.resumeWithException(ex)
                        }
                    })
        }
    }

    /**
     * DeviceListAttribute
     *
     * ```
     * For example, on endpoint 0:
     *   device: [long type: 22, int revision: 1] -> maps to Root node (0x0016) (utility device type)
     * on endpoint 1:
     *   device: [long type: 256, int revision: 1] -> maps to On/Off Light (0x0100)
     * ```
     */
    suspend fun readDescriptorClusterDeviceListAttribute(
        devicePtr: Long,
        endpoint: Int
    ): List<ChipStructs.DescriptorClusterDeviceTypeStruct> {
        return suspendCancellableCoroutine { continuation ->
            getDescriptorClusterForDevice(devicePtr, endpoint)
                .readDeviceTypeListAttribute(
                    object : ChipClusters.DescriptorCluster.DeviceTypeListAttributeCallback {
                        override fun onSuccess(
                            values: List<ChipStructs.DescriptorClusterDeviceTypeStruct>
                        ) {
                            continuation.resume(values)
                        }

                        override fun onError(ex: Exception) {
                            continuation.resumeWithException(ex)
                        }
                    })
        }
    }

    /**
     * ServerListAttribute See
     * https://github.com/project-chip/connectedhomeip/blob/master/zzz_generated/app-common/app-common/zap-generated/ids/Clusters.h
     *
     * ```
     * For example: on endpoint 0
     *     sendReadServerListAttribute: [3]
     *     sendReadServerListAttribute: [4]
     *     sendReadServerListAttribute: [29]
     *     ... and more ...
     * on endpoint 1:
     *     sendReadServerListAttribute: [3]
     *     sendReadServerListAttribute: [4]
     *     sendReadServerListAttribute: [5]
     *     sendReadServerListAttribute: [6]
     *     sendReadServerListAttribute: [7]
     *     ... and more ...
     * on endpoint 2:
     *     sendReadServerListAttribute: [4]
     *     sendReadServerListAttribute: [6]
     *     sendReadServerListAttribute: [29]
     *     sendReadServerListAttribute: [1030]
     *
     * Some mappings:
     *     namespace Groups = 0x00000004 (4)
     *     namespace OnOff = 0x00000006 (6)
     *     namespace Descriptor = 0x0000001D (29)
     *     namespace OccupancySensing = 0x00000406 (1030)
     * ```
     */
    suspend fun readDescriptorClusterServerListAttribute(
        devicePtr: Long,
        endpoint: Int
    ): List<Long> {
        return suspendCancellableCoroutine { continuation ->
            getDescriptorClusterForDevice(devicePtr, endpoint)
                .readServerListAttribute(
                    object : ChipClusters.DescriptorCluster.ServerListAttributeCallback {
                        override fun onSuccess(values: MutableList<Long>) {
                            continuation.resume(values)
                        }

                        override fun onError(ex: Exception) {
                            continuation.resumeWithException(ex)
                        }
                    })
        }
    }

    /** ClientListAttribute */
    suspend fun readDescriptorClusterClientListAttribute(
        devicePtr: Long,
        endpoint: Int
    ): List<Long> {
        return suspendCancellableCoroutine { continuation ->
            getDescriptorClusterForDevice(devicePtr, endpoint)
                .readClientListAttribute(
                    object : ChipClusters.DescriptorCluster.ClientListAttributeCallback {
                        override fun onSuccess(values: MutableList<Long>) {
                            continuation.resume(values)
                        }

                        override fun onError(ex: Exception) {
                            continuation.resumeWithException(ex)
                        }
                    })
        }
    }

    private fun getDescriptorClusterForDevice(
        devicePtr: Long,
        endpoint: Int
    ): ChipClusters.DescriptorCluster {
        return ChipClusters.DescriptorCluster(devicePtr, endpoint)
    }

    // -----------------------------------------------------------------------------------------------
    // ApplicationCluster functions

    suspend fun readApplicationBasicClusterAttributeList(
        deviceId: Long,
        endpoint: Int
    ): List<Long> {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return emptyList()
            }
        return suspendCancellableCoroutine { continuation ->
            getApplicationBasicClusterForDevice(connectedDevicePtr, endpoint)
                .readAttributeListAttribute(
                    object : ChipClusters.ApplicationBasicCluster.AttributeListAttributeCallback {
                        override fun onSuccess(value: MutableList<Long>) {
                            continuation.resume(value)
                        }

                        override fun onError(ex: Exception) {
                            continuation.resumeWithException(ex)
                        }
                    })
        }
    }

    private fun getApplicationBasicClusterForDevice(
        devicePtr: Long,
        endpoint: Int
    ): ChipClusters.ApplicationBasicCluster {
        return ChipClusters.ApplicationBasicCluster(devicePtr, endpoint)
    }

    // -----------------------------------------------------------------------------------------------
    // BasicCluster functions

    suspend fun readBasicClusterVendorIDAttribute(deviceId: Long, endpoint: Int): Int? {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return null
            }
        return suspendCancellableCoroutine { continuation ->
            getBasicClusterForDevice(connectedDevicePtr, endpoint)
                .readVendorIDAttribute(
                    object : ChipClusters.ApplicationBasicCluster.VendorIDAttributeCallback {
                        override fun onSuccess(value: Int?) {
                            continuation.resume(value)
                        }

                        override fun onError(ex: Exception) {
                            continuation.resumeWithException(ex)
                        }
                    })
        }
    }

    suspend fun readBasicClusterAttributeList(deviceId: Long, endpoint: Int): List<Long> {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return emptyList()
            }

        return suspendCancellableCoroutine { continuation ->
            getBasicClusterForDevice(connectedDevicePtr, endpoint)
                .readAttributeListAttribute(
                    object : ChipClusters.ApplicationBasicCluster.AttributeListAttributeCallback {
                        override fun onSuccess(values: MutableList<Long>) {
                            continuation.resume(values)
                        }

                        override fun onError(ex: Exception) {
                            continuation.resumeWithException(ex)
                        }
                    })
        }
    }

    private fun getBasicClusterForDevice(
        devicePtr: Long,
        endpoint: Int
    ): ChipClusters.ApplicationBasicCluster {
        return ChipClusters.ApplicationBasicCluster(devicePtr, endpoint)
    }

    /**
     * Writes NodeLabel attribute. See spec section "11.1.6.3. Attributes" of the "Basic Information
     * Cluster".
     *
     * @param deviceId device identifier
     * @param nodeLabel device name/node label
     */
    suspend fun writeBasicClusterNodeLabelAttribute(deviceId: Long, nodeLabel: String) {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return
            }

        return suspendCancellableCoroutine { continuation ->
            val callback =
                object : ChipClusters.DefaultClusterCallback {
                    override fun onSuccess() {
                        continuation.resume(Unit)
                    }

                    override fun onError(ex: Exception) {
                        continuation.resumeWithException(ex)
                    }
                }

            ChipClusters.BasicInformationCluster(connectedDevicePtr, 0)
                .writeNodeLabelAttribute(callback, nodeLabel)
        }
    }

    /**
     * Reads the vendor name attribute. See spec section "11.1.6.3. Attributes" of the "Basic
     * Information Cluster".
     *
     * @param deviceId the device identifier.
     * @return the vendor name
     */
    suspend fun readBasicClusterVendorNameAttribute(deviceId: Long): String {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return ""
            }

        return suspendCancellableCoroutine { continuation ->
            val callback =
                object : ChipClusters.CharStringAttributeCallback {
                    override fun onSuccess(value: String) {
                        continuation.resume(value)
                    }

                    override fun onError(ex: Exception) {
                        continuation.resumeWithException(ex)
                    }
                }

            ChipClusters.BasicInformationCluster(connectedDevicePtr, 0)
                .readVendorNameAttribute(callback)
        }
    }

    /**
     * Reads node's product name attribute. See spec section "11.1.6.3. Attributes" of the "Basic
     * Information Cluster".
     *
     * @param deviceId the device identifier
     * @return the product name
     */
    suspend fun readBasicClusterProductNameAttribute(deviceId: Long): String {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return ""
            }

        return suspendCancellableCoroutine { continuation ->
            val callback =
                object : ChipClusters.CharStringAttributeCallback {
                    override fun onSuccess(value: String) {
                        continuation.resume(value)
                    }

                    override fun onError(ex: Exception) {
                        continuation.resumeWithException(ex)
                    }
                }

            ChipClusters.BasicInformationCluster(connectedDevicePtr, 0)
                .readProductNameAttribute(callback)
        }
    }

    /**
     * Reads NodeLabel attribute. See spec section "11.1.6.3. Attributes" of the "Basic Information
     * Cluster".
     *
     * @param deviceId device identifier
     * @return the NodeLabel
     */
    suspend fun readBasicClusterNodeLabelAttribute(deviceId: Long): String? {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return null
            }

        return suspendCancellableCoroutine { continuation ->
            val callback =
                object : ChipClusters.CharStringAttributeCallback {
                    override fun onSuccess(value: String?) {
                        continuation.resume(value)
                    }

                    override fun onError(ex: Exception) {
                        continuation.resumeWithException(ex)
                    }
                }

            ChipClusters.BasicInformationCluster(connectedDevicePtr, 0)
                .readNodeLabelAttribute(callback)
        }
    }

    // -----------------------------------------------------------------------------------------------
    // OnOffCluster functions

    // CODELAB FEATURED BEGIN
    suspend fun toggleDeviceStateOnOffCluster(deviceId: Long, endpoint: Int) {
        Napier.d { "AAA, toggleDeviceStateOnOffCluster())" }
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return
            }
        return suspendCancellableCoroutine { continuation ->
            getOnOffClusterForDevice(connectedDevicePtr, endpoint)
                .toggle(
                    object : ChipClusters.DefaultClusterCallback {
                        override fun onSuccess() {
                            continuation.resume(Unit)
                        }

                        override fun onError(ex: Exception) {
                            Napier.e(ex) { "AAA, readOnOffAttribute command failure" }
                            continuation.resumeWithException(ex)
                        }
                    })
        }
    }

    // CODELAB FEATURED END

    suspend fun setOnOffDeviceStateOnOffCluster(deviceId: Long, isOn: Boolean, endpoint: Int) {
        Napier.d {
            "AAA, setOnOffDeviceStateOnOffCluster() [${deviceId}] isOn [${isOn}] endpoint [${endpoint}]"
        }
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return
            }
        if (isOn) {
            // ON
            return suspendCancellableCoroutine { continuation ->
                getOnOffClusterForDevice(connectedDevicePtr, endpoint)
                    .on(
                        object : ChipClusters.DefaultClusterCallback {
                            override fun onSuccess() {
                                Napier.d { "AAA, Success for setOnOffDeviceStateOnOffCluster" }
                                continuation.resume(Unit)
                            }

                            override fun onError(ex: Exception) {
                                Napier.e(ex) { "AAA, Failure for setOnOffDeviceStateOnOffCluster, ${ex.localizedMessage}" }
                                continuation.resumeWithException(ex)
                            }
                        })
            }
        } else {
            // OFF
            return suspendCancellableCoroutine { continuation ->
                getOnOffClusterForDevice(connectedDevicePtr, endpoint)
                    .off(
                        object : ChipClusters.DefaultClusterCallback {
                            override fun onSuccess() {
                                Napier.d { "AAA, Success for getOnOffDeviceStateOnOffCluster" }
                                continuation.resume(Unit)
                            }

                            override fun onError(ex: Exception) {
                                Napier.e(ex) { "AAA, Failure for getOnOffDeviceStateOnOffCluster" }
                                continuation.resumeWithException(ex)
                            }
                        })
            }
        }
    }

    suspend fun lockUnlockDoor(
        deviceId: Long,
        isLocked: Boolean,
        endpoint: Int,
        pinCode: String? = null
    ) {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return
            }

        // If pin code is not provided then pull empty value.
        val pinOptional = pinCode?.let {
            Optional.of(it.toByteArray(Charsets.UTF_8))
        } ?: Optional.empty()

        return suspendCancellableCoroutine { continuation ->
            val cluster = getLockUnlockClusterForDevice(connectedDevicePtr, endpoint)
            val callback = object : ChipClusters.DefaultClusterCallback {
                override fun onSuccess() {
                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onError(ex: Exception?) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            ex ?: RuntimeException("Unknown Matter Error")
                        )
                    }
                }
            }

            if (isLocked) {
                cluster.lockDoor(callback, pinOptional, 10000)
            } else {
                cluster.unlockDoor(callback, pinOptional, 10000)
            }
        }
    }

    suspend fun getDeviceStateOnOffCluster(deviceId: Long, endpoint: Int): Boolean? {
        Napier.d { "AAA, getDeviceStateOnOffCluster())" }
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return null
            }
        return suspendCancellableCoroutine { continuation ->
            getOnOffClusterForDevice(connectedDevicePtr, endpoint)
                .readOnOffAttribute(
                    object : ChipClusters.BooleanAttributeCallback {
                        override fun onSuccess(value: Boolean) {
                            Napier.d { "AAA, readOnOffAttribute success: [$value]" }
                            continuation.resume(value)
                        }

                        override fun onError(ex: Exception) {
                            Napier.e(ex) {
                                "AAA, readOnOffAttribute command failure, ${ex.localizedMessage}"
                            }
                            continuation.resumeWithException(ex)
                        }
                    })
        }
    }

    private fun getOnOffClusterForDevice(
        devicePtr: Long,
        endpoint: Int
    ): ChipClusters.OnOffCluster {
        return ChipClusters.OnOffCluster(devicePtr, endpoint)
    }

    private fun getLockUnlockClusterForDevice(
        devicePtr: Long,
        endpoint: Int
    ): ChipClusters.DoorLockCluster {
        return ChipClusters.DoorLockCluster(devicePtr, endpoint)
    }
}