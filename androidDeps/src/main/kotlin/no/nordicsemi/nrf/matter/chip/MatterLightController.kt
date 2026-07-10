package no.nordicsemi.nrf.matter.chip

import chip.devicecontroller.ChipClusters
import chip.devicecontroller.ReportCallback
import chip.devicecontroller.model.ChipAttributePath
import chip.devicecontroller.model.ChipEventPath
import chip.devicecontroller.model.NodeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.DeviceId
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

/**
 * Controls and observes the state of a Matter light endpoint via the On/Off and Level Control
 * clusters.
 *
 * All commands are sent over an existing CASE session; callers are responsible for ensuring the
 * device has already been commissioned and is reachable through [chipClient].
 *
 * @property chipClient the underlying Matter stack used to resolve device pointers and send/subscribe
 * to cluster attributes.
 */
class MatterLightController(
    private val chipClient: ChipClient,
) {
    /**
     * Writes the current brightness level to the Level Control cluster.
     *
     * The command used is "Move to Level with On/Off", which sets the brightness level and turns on
     * or off the device based on the brightness level (if [brightnessLevel] > 0, the device will be
     * turned on; if [brightnessLevel] == 0, the device will be turned off). The transition is
     * instantaneous (no fade).
     *
     * @param deviceId the commissioned device to control.
     * @param brightnessLevel target level in the device's raw Level Control range (typically 1-254).
     * @param endpoint the Matter endpoint exposing the Level Control cluster.
     * @throws Exception if the underlying cluster command fails (e.g. device unreachable, command
     * rejected).
     */
    suspend fun setBrightnessLevel(
        deviceId: DeviceId,
        brightnessLevel: Int,
        endpoint: Int
    ) {
        val connectedDevicePtr = getConnectedDevicePointerOrNull(deviceId) ?: return
        awaitClusterCallback { callback ->
            getLevelControlClusterForDevice(connectedDevicePtr, endpoint)
                .moveToLevelWithOnOff(
                    callback,
                    brightnessLevel,
                    0, // transitionTime (0 = instantaneous)
                    0, // optionsMask
                    0  // optionsOverride
                )
        }
    }

    /**
     * Turns the light on or off via the On/Off cluster.
     *
     * @param deviceId the commissioned device to control.
     * @param isOn `true` to send the On command, `false` to send the Off command.
     * @param endpoint the Matter endpoint exposing the On/Off cluster.
     * @throws Exception if the underlying cluster command fails (e.g. device unreachable, command
     * rejected).
     */
    suspend fun setOnOffDeviceStateOnOffCluster(deviceId: DeviceId, isOn: Boolean, endpoint: Int) {
        val connectedDevicePtr = getConnectedDevicePointerOrNull(deviceId) ?: return
        val cluster = getOnOffClusterForDevice(connectedDevicePtr, endpoint)
        awaitClusterCallback { callback ->
            if (isOn) cluster.on(callback) else cluster.off(callback)
        }
    }

    /**
     * Subscribes to the On/Off attribute of a light endpoint and emits its state as it changes.
     *
     * The subscription reports changes instantly and otherwise sends a heartbeat every 10 seconds;
     * establishing the underlying session is subject to a 10 second timeout. The returned [Flow]
     * closes with an exception if the subscription cannot be established.
     *
     * @param deviceId the commissioned device to observe.
     * @param endpoint the Matter endpoint exposing the On/Off cluster.
     * @return a cold [Flow] emitting `true` when the light is on, `false` when it is off.
     */
    fun observeLightState(deviceId: DeviceId, endpoint: Int): Flow<Boolean> =
        observeAttribute(
            deviceId = deviceId,
            endpoint = endpoint,
            clusterId = ON_OFF_CLUSTER_ID,
            attributeId = ON_OFF_ATTRIBUTE_ID,
        ) { rawValue ->
            (rawValue as? Boolean)?.also {
                NordicLogger.info("Received On/Off report: isLedOn=$it", tag = TAG)
            }
        }

    /**
     * Subscribes to the CurrentLevel attribute of a light endpoint and emits its brightness as it
     * changes.
     *
     * The raw device level (1-254) is normalized to a 0f-1f percentage before being emitted. The
     * subscription reports changes instantly and otherwise sends a heartbeat every 10 seconds;
     * establishing the underlying session is subject to a 10 second timeout. The returned [Flow]
     * closes with an exception if the subscription cannot be established.
     *
     * @param deviceId the commissioned device to observe.
     * @param endpoint the Matter endpoint exposing the Level Control cluster.
     * @return a cold [Flow] emitting brightness as a fraction between 0f (off) and 1f (max).
     */
    fun observeBrightnessState(deviceId: DeviceId, endpoint: Int): Flow<Float> =
        observeAttribute(
            deviceId = deviceId,
            endpoint = endpoint,
            clusterId = LEVEL_CONTROL_CLUSTER_ID,
            attributeId = CURRENT_LEVEL_ATTRIBUTE_ID,
        ) { rawValue ->
            (rawValue as? Number)?.let { level ->
                ((level.toFloat() - MIN_LEVEL) / LEVEL_RANGE).coerceIn(0f, 1f)
            }?.also {
                NordicLogger.info("Received Brightness report: brightnessPercentage=$it", tag = TAG)
            }
        }

    private suspend fun getConnectedDevicePointerOrNull(deviceId: DeviceId): Long? =
        try {
            connectedDevicePointer(deviceId)
        } catch (e: IllegalStateException) {
            NordicLogger.error("Can't get connectedDevicePointer.", e, tag = TAG)
            null
        }

    /**
     * Runs a cluster command that reports completion via [ChipClusters.DefaultClusterCallback],
     * suspending until it succeeds or fails.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun awaitClusterCallback(
        command: (ChipClusters.DefaultClusterCallback) -> Unit
    ) = suspendCancellableCoroutine { continuation ->
        command(object : ChipClusters.DefaultClusterCallback {
            override fun onSuccess() {
                continuation.resume(Unit, onCancellation = {})
            }

            override fun onError(ex: Exception) {
                continuation.resumeWithException(ex)
            }
        })
    }

    private fun getLevelControlClusterForDevice(
        devicePtr: Long,
        endpoint: Int
    ): ChipClusters.LevelControlCluster {
        return ChipClusters.LevelControlCluster(devicePtr, endpoint)
    }

    private fun getOnOffClusterForDevice(
        devicePtr: Long,
        endpoint: Int
    ): ChipClusters.OnOffCluster {
        return ChipClusters.OnOffCluster(devicePtr, endpoint)
    }

    /**
     * Subscribes to a single attribute and maps each report to [T], emitting only non-null results.
     */
    private fun <T> observeAttribute(
        deviceId: DeviceId,
        endpoint: Int,
        clusterId: Long,
        attributeId: Long,
        mapValue: (Any?) -> T?,
    ): Flow<T> = callbackFlow {
        val reportCallback = object : ReportCallback {
            override fun onError(
                attributePath: ChipAttributePath?,
                eventPath: ChipEventPath?,
                e: Exception
            ) {
                NordicLogger.error(
                    "Error receiving report from DK for path: $attributePath", e, tag = TAG
                )
            }

            override fun onReport(nodeState: NodeState) {
                val endpointState = nodeState.getEndpointState(endpoint) ?: return
                val rawValue = endpointState.getClusterState(clusterId)?.getAttributeState(attributeId)?.value
                val mappedValue = mapValue(rawValue) ?: return
                trySend(mappedValue)
            }
        }

        try {
            val devicePtr = connectedDevicePointer(deviceId)
            chipClient.subscribeAttribute(
                reportCallback = reportCallback,
                devicePtr = devicePtr,
                attributePaths = listOf(ChipAttributePath.newInstance(endpoint, clusterId, attributeId)),
                minIntervalS = 0,    // Report changes instantly
                maxIntervalS = 10,   // Heartbeat check every 10 seconds
                timeoutMs = 10000    // 10 second network timeout for establishing the session
            )
        } catch (e: Exception) {
            NordicLogger.error("Failed to setup wrapper subscription", e, tag = TAG)
            close(e)
        }

        awaitClose {
            // Handle stream cleanup
        }
    }

    private suspend fun connectedDevicePointer(deviceId: DeviceId): Long =
        chipClient.getConnectedDevicePointer(deviceId.longValue)

    companion object {
        private const val ON_OFF_CLUSTER_ID = 6L
        private const val ON_OFF_ATTRIBUTE_ID = 0L
        private const val LEVEL_CONTROL_CLUSTER_ID = 8L
        private const val CURRENT_LEVEL_ATTRIBUTE_ID = 0L
        private const val MIN_LEVEL = 1f
        private const val LEVEL_RANGE = 253f

        private val TAG: String
            get() = "MatterLightController"
    }
}