package no.nordicsemi.nrf.matter.chip

import chip.devicecontroller.ReportCallback
import chip.devicecontroller.model.ChipAttributePath
import chip.devicecontroller.model.ChipEventPath
import chip.devicecontroller.model.NodeState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.DeviceId

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

class MatterManufacturerSpecificController(
    private val chipClient: ChipClient,
) {
    /**
     * Sends the vendor-specific "set LED" command.
     *
     * The command always targets [LED_ENDPOINT] using the fixed [MANUFACTURER_SPECIFIC_CLUSTER_ID] /
     * [SET_LED_COMMAND_ID] pair; the endpoint, cluster ID and command ID are not resolved from the
     * device's descriptor cluster, so this only works against a device that exposes this exact
     * cluster/command combination on that endpoint.
     *
     * @param deviceId the commissioned device to control.
     * @throws IllegalStateException if the device pointer cannot be resolved (e.g. device
     * unreachable).
     */
    suspend fun setLed(deviceId: DeviceId) {
        chipClient.setLet(
            deviceId,
            LED_ENDPOINT,
            clusterId = MANUFACTURER_SPECIFIC_CLUSTER_ID,
            commandId = SET_LED_COMMAND_ID,
        )
    }

    /**
     * Subscribes to the vendor-specific button-pressed attribute and emits its state as it
     * changes.
     *
     * The subscription reports changes instantly and otherwise sends a heartbeat every 30 seconds;
     * establishing the underlying session is subject to a 30-second timeout. The returned [Flow]
     * closes with an exception if the subscription cannot be established.
     *
     * @param deviceId the commissioned device to observe.
     * @param endpoint the Matter endpoint exposing the manufacturer-specific cluster.
     * @return a cold [Flow] emitting `true` when the button is pressed, `false` when released.
     */
    fun observeButtonChanges(deviceId: DeviceId, endpoint: Int): Flow<Boolean> = callbackFlow {
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
                val isPressed = endpointState.getClusterState(MANUFACTURER_SPECIFIC_CLUSTER_ID)
                    ?.getAttributeState(BUTTON_ATTRIBUTE_ID)?.value as? Boolean ?: return
                NordicLogger.info("Received button report: isPressed=$isPressed", tag = TAG)
                trySend(isPressed)
            }
        }

        try {
            val devicePtr = connectedDevicePointer(deviceId)
            chipClient.subscribeAttribute(
                reportCallback = reportCallback,
                devicePtr = devicePtr,
                attributePaths = listOf(
                    ChipAttributePath.newInstance(endpoint, MANUFACTURER_SPECIFIC_CLUSTER_ID, BUTTON_ATTRIBUTE_ID)
                ),
                minIntervalS = 0,    // Report changes instantly
                maxIntervalS = 30,   // Heartbeat check every 30 seconds
                timeoutMs = 30000    // 30 second network timeout for establishing the session
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
        private const val MANUFACTURER_SPECIFIC_CLUSTER_ID = 0xFFF1FC01L
        private const val SET_LED_COMMAND_ID = 0xFFF10000L
        private const val BUTTON_ATTRIBUTE_ID = 0xFFF10002L
        private const val LED_ENDPOINT = 0x1

        private val TAG: String
            get() = "MatterManufacturerSpecificController"
    }
}