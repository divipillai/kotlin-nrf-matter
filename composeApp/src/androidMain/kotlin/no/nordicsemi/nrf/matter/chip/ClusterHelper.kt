package no.nordicsemi.nrf.matter.chip

import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ChipStructs
import chip.devicecontroller.model.AttributeState
import chip.devicecontroller.model.ChipAttributePath
import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.domain.ManufacturerSpecificData
import no.nordicsemi.nrf.matter.model.DeviceId
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

    /** Fetches MatterDeviceInfo for each endpoint supported by the device. */
    suspend fun fetchDeviceMatterInfo(deviceId: DeviceId): List<DeviceMatterInfo> {
        Napier.d { "AAA, fetchDevicet()MatterInfo(): deviceId [${deviceId}]" }
        val matterDeviceInfoList = arrayListOf<DeviceMatterInfo>()
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId.longValue)
            } catch (e: IllegalStateException) {
                Napier.e(e) { "AAA, Can't get connectedDevicePointer." }
                return emptyList()
            }
        fetchDeviceMatterInfo(deviceId.longValue, connectedDevicePtr, 0, matterDeviceInfoList)
        return matterDeviceInfoList
    }

    /** Fetches MatterDeviceInfo for a specific endpoint. */
    private suspend fun fetchDeviceMatterInfo(
        nodeId: Long,
        connectedDevicePtr: Long,
        endpointInt: Int,
        matterDeviceInfoList: ArrayList<DeviceMatterInfo>
    ) {

        Napier.d { "AAA, fetchDeviceMatterInfo(): nodeId [$nodeId] endpoint [$endpointInt]" }

        val partsListAttribute =
            readDescriptorClusterPartsListAttribute(connectedDevicePtr, endpointInt)
        Napier.d { "AAA, partsListAttribute [$partsListAttribute]" }

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

        // manufacturer specific
        Napier.i { "AAATESTAAA, serverClusters: $serverClusters" }
        val manufacturerSpecificData = if (serverListAttribute.contains(0xFFF1FC01)) {
            Napier.i { "AAATESTAAA, get manufacturer data" }
            getManufacturerSpecificData(endpointInt.toLong(), connectedDevicePtr)
        } else {
            Napier.i { "AAATESTAAA, no manufcaturer specific cluster" }
            null
        }

        val deviceMatterInfo = DeviceMatterInfo(
            endpointInt,
            types,
            serverClusters,
            clientClusters,
            manufacturerSpecificData
        )
        matterDeviceInfoList.add(deviceMatterInfo)

        // Recursive call for the parts supported by the endpoint.
        // For each part (endpoint)
        partsListAttribute?.forEach { part ->
            Napier.d { "AAA, part [$part] is [${part.javaClass}]" }
            val childEndpoint = part as? Int ?: return@forEach
            Napier.d { "AAA, Processing part [$part]" }

            val childServerList = try {
                readDescriptorClusterServerListAttribute(connectedDevicePtr, childEndpoint)
            } catch (t: Throwable) {
                Napier.w("AAA, Endpoint $childEndpoint has no Descriptor cluster, skipping")
                return@forEach
            }

            if (childServerList.isNotEmpty()) {
                fetchDeviceMatterInfo(
                    nodeId,
                    connectedDevicePtr,
                    childEndpoint,
                    matterDeviceInfoList
                )
            }
        }
    }

    // -----------------------------------------------------------------------------------------------
    // DescriptorCluster functions

    /**
     * PartsListAttribute. These are the endpoints supported.
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

    suspend fun getManufacturerSpecificData(
        endpoint: Long,
        connectedDevicePtr: Long
    ): ManufacturerSpecificData? {
        Napier.d("getManufacturerSpecificData called.")
        return try {
            val ep = endpoint.toInt()

            Napier.d("endpoint: $ep", tag = "AAA")
            val namePath = ChipAttributePath.newInstance(ep, 0xFFF1FC01, 0xFFF10000)
            val ledPath = ChipAttributePath.newInstance(ep, 0xFFF1FC01, 0xFFF10001)
            val buttonPath = ChipAttributePath.newInstance(ep, 0xFFF1FC01, 0xFFF10002)
            Napier.d("namePath: $namePath, ledPath: $ledPath, buttonPath: $buttonPath", tag = "AAA")
            val results = chipClient.readAttributes(
                connectedDevicePtr,
                listOf(namePath, ledPath, buttonPath)
            )

            // Look up by matching IDs instead of by path object reference
            fun Map<ChipAttributePath, AttributeState>.findValue(
                endpointId: Int,
                clusterId: Long,
                attributeId: Long
            ): AttributeState? = entries.firstOrNull { (path, _) ->
                path.endpointId.id.toInt() == endpointId &&
                        path.clusterId.id == clusterId &&
                        path.attributeId.id == attributeId
            }?.value

            val name =
                results.findValue(ep, 0xFFF1FC01L, 0xFFF10000L)?.value as? String ?: return null
            val led = results.findValue(ep, 0xFFF1FC01L, 0xFFF10001L)?.value as? Boolean ?: false
            val button = results.findValue(ep, 0xFFF1FC01L, 0xFFF10002L)?.value as? Boolean ?: false

            Napier.d("AAA, name=$name led=$led button=$button", tag = "AAA")

            ManufacturerSpecificData(name, led, button)
        } catch (t: Throwable) {
            Napier.e("AAA, getManufacturerSpecificData failed: ${t.message}", tag = "AAA")
            null
        }
    }

    suspend fun generateRandomNumber(deviceId: DeviceId): Int? {
        return try {
            val connectedDevicePtr = chipClient.getConnectedDevicePointer(deviceId.longValue)
            chipClient.invokeCommand(
                deviceId,
                ChipAttributePath.newInstance(
                    0,
                    0x28,
                    0x00,
                )

            )
            val namePath = ChipAttributePath.newInstance(0, 0x0028, 0x00017)
            val nameAttr = chipClient.readAttribute(connectedDevicePtr, namePath)
            nameAttr?.value as? Int
        } catch (t: Throwable) {
            t.printStackTrace()
            null
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
     * ServerListAttribute
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

    /**
     * Writes NodeLabel attribute. See spec section "11.1.6.3. Attributes" of the "Basic Information
     * Cluster".
     *
     * @param deviceId device identifier
     * @param nodeLabel device name/node label
     */
    suspend fun writeBasicClusterNodeLabelAttribute(deviceId: DeviceId, nodeLabel: String) {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId.longValue)
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
    suspend fun readBasicClusterVendorNameAttribute(deviceId: DeviceId): String {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId.longValue)
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

    suspend fun readSoftwareVersionAttribute(deviceId: DeviceId): String? {
        return try {
            val connectedDevicePtr = chipClient.getConnectedDevicePointer(deviceId.longValue)
            val namePath = ChipAttributePath.newInstance(0, 0x0028, 0x000A)
            val nameAttr = chipClient.readAttribute(connectedDevicePtr, namePath)
            nameAttr?.value as? String
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    /**
     * Reads node's product name attribute. See spec section "11.1.6.3. Attributes" of the "Basic
     * Information Cluster".
     *
     * @param deviceId the device identifier
     * @return the product name
     */
    suspend fun readBasicClusterProductNameAttribute(deviceId: DeviceId): String {
        val connectedDevicePtr =
            try {
                chipClient.getConnectedDevicePointer(deviceId.longValue)
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

    suspend fun onOffSwitch(
        deviceId: Long,
        isSwitchOn: Boolean,
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
            val cluster = getOnOffSwitchClusterForDevice(connectedDevicePtr, endpoint)
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

            if (isSwitchOn) {
                // TODO: implement this to on switch cluster.

            } else {
                // TODO: implement this to off switch cluster.
            }
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

    private fun getOnOffSwitchClusterForDevice(
        devicePtr: Long,
        endpoint: Int
    ): ChipClusters.OnOffCluster {
        return ChipClusters.OnOffCluster(devicePtr, endpoint)
    }

}