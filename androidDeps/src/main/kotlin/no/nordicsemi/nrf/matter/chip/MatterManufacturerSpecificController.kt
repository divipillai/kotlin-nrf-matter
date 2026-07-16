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

/**
 * Controls and observes the state of a Matter device's vendor/manufacturer-specific cluster
 * (cluster ID `0xFFF1FC01`).
 *
 * All commands are sent over an existing CASE session; callers are responsible for ensuring the
 * device has already been commissioned and is reachable through [chipClient].
 *
 * ### About the cluster ID
 * Matter reserves cluster IDs `0xFC00`-`0xFFFE` (per vendor) for manufacturer-specific
 * extensions, so a vendor cluster ID is conventionally built as `0xVVVVFCxx`, where `VVVV` is the
 * 16-bit CSA-assigned Vendor ID and `FCxx` is a vendor-chosen suffix in that reserved range. The
 * cluster ID used here, `0xFFF1FC01`, decomposes into Vendor ID `0xFFF1` and suffix `0xFC01`. The
 * attribute/command IDs (`0xFFF10000`-`0xFFF10002`) follow the same `0xVVVVxxxx` shape.
 *
 * `0xFFF1` is the Vendor ID nRF Connect SDK Matter samples ship with by default (confirmed on
 * [Nordic DevZone](https://devzone.nordicsemi.com/f/nordic-q-a/91101/how-do-i-change-the-vendor-id)).
 * Note the nuance: `0xFFF1` is also documented elsewhere as the Matter specification's generic
 * *test* Vendor ID used by unbranded sample templates across multiple vendor SDKs, not a value the
 * CSA registry lists as exclusively assigned to Nordic — so treat it as "what Nordic's samples
 * default to" rather than "Nordic's registered production VID" unless confirmed otherwise. See the
 * [Nordic DevZone thread on manufacturer-specific clusters](https://devzone.nordicsemi.com/f/nordic-q-a/99501/matter-manufacturer-specific-cluster)
 * for a worked (if messy) example of defining one of these clusters in firmware.
 *
 * ### Preparing matching firmware (general nRF Connect SDK / ZAP workflow)
 * To expose a compatible cluster from firmware (Zephyr + nRF Connect SDK Matter sample):
 * 1. Add the cluster/attribute/command definitions (name, type, IDs) to the sample's ZAP cluster
 *    data, either via the ZAP GUI (`west zap-gui`) editing the sample's `.zap`/`.matter` file, or
 *    by hand-editing the underlying ZAP data model XML.
 * 2. Regenerate the `zap-generated` C++ callback stubs (`west build` triggers code generation, or
 *    run the ZAP generation script directly) so the new cluster gets server/client interaction
 *    model glue code.
 * 3. Implement the generated attribute read/write and command-invoke callbacks in the sample's
 *    application code (e.g. `main.cpp` / the device's cluster server logic), backing them with
 *    real hardware (LED GPIO, button GPIO, etc.).
 * 4. Instantiate the cluster on the desired endpoint in the generated endpoint config
 *    (`zap-generated/endpoint_config.h`), so it shows up in that endpoint's server cluster list.
 * 5. Rebuild and flash the firmware; the resulting device will report this cluster ID in its
 *    Descriptor cluster's ServerList for that endpoint.
 *
 * Once firmware is flashed, the exact numeric cluster/attribute/command IDs it uses must be
 * copied into this class's companion object constants — there is no IDL/schema exchange at
 * runtime, so a mismatch here silently fails (commands time out / attributes read back `null`).
 *
 * @property chipClient the underlying Matter stack used to resolve device pointers and send/subscribe
 * to cluster attributes.
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
     * Sends a vendor-specific "generate random number" invoke, then reads back the resulting
     * value.
     *
     * Targets cluster [RANDOM_NUMBER_CLUSTER_ID] (Basic Information) on endpoint
     * [RANDOM_NUMBER_ENDPOINT], invoking [RANDOM_NUMBER_COMMAND_ID] and then reading
     * [RANDOM_NUMBER_ATTRIBUTE_ID] for the result. These IDs are fixed rather than resolved from
     * the device's descriptor cluster, and depend on the companion firmware exposing this vendor
     * command on that cluster.
     *
     * @param deviceId the commissioned device to invoke.
     * @return the generated random number, or `null` if the device is unreachable, the command is
     * rejected, or the attribute can't be read back.
     */
    suspend fun generateRandomNumber(deviceId: DeviceId): Long? {
        return try {
            val connectedDevicePtr = connectedDevicePointer(deviceId)
            chipClient.generateRandomNumber(
                connectedDevicePtr,
                ChipAttributePath.newInstance(
                    RANDOM_NUMBER_ENDPOINT,
                    RANDOM_NUMBER_CLUSTER_ID,
                    RANDOM_NUMBER_COMMAND_ID,
                )
            )
            val namePath = ChipAttributePath.newInstance(
                RANDOM_NUMBER_ENDPOINT,
                RANDOM_NUMBER_CLUSTER_ID,
                RANDOM_NUMBER_ATTRIBUTE_ID,
            )
            val nameAttr = chipClient.readAttribute(connectedDevicePtr, namePath)
            nameAttr?.value as? Long
        } catch (t: Throwable) {
            NordicLogger.error("Random number generation failed: ${t.message}", t, tag = TAG)
            t.printStackTrace()
            null
        }
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

        private const val RANDOM_NUMBER_ENDPOINT = 0
        private const val RANDOM_NUMBER_CLUSTER_ID = 0x28L
        private const val RANDOM_NUMBER_COMMAND_ID = 0x00L
        private const val RANDOM_NUMBER_ATTRIBUTE_ID = 0x00017L

        private val TAG: String
            get() = "MatterManufacturerSpecificController"
    }
}