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
import no.nordicsemi.nrf.matter.model.LockDeviceState
import java.util.Optional
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
 * Controls and observes the state of a Matter door lock endpoint via the Door Lock cluster.
 *
 * All commands are sent over an existing CASE session; callers are responsible for ensuring the
 * device has already been commissioned and is reachable through [chipClient].
 *
 * @property chipClient the underlying Matter stack used to resolve device pointers and send/subscribe
 * to cluster attributes.
 */
class MatterDoorLockController(
    private val chipClient: ChipClient,
) {
    /**
     * Locks or unlocks the door via the Door Lock cluster.
     *
     * @param deviceId the commissioned device to control.
     * @param isLocked `true` to send the Lock Door command, `false` to send the Unlock Door command.
     * @param endpoint the Matter endpoint exposing the Door Lock cluster.
     * @param pinCode optional PIN code required by the lock to authorize the operation; when
     * omitted, an empty PIN is sent.
     * @throws Exception if the underlying cluster command fails (e.g. device unreachable, command
     * rejected).
     */
    suspend fun lockUnlockDoor(
        deviceId: DeviceId,
        isLocked: Boolean,
        endpoint: Int,
        pinCode: String? = null,
    ) {
        val connectedDevicePtr = getConnectedDevicePointerOrNull(deviceId) ?: return
        val pinOptional = pinCode?.let {
            Optional.of(it.toByteArray(Charsets.UTF_8))
        } ?: Optional.empty()

        awaitClusterCallback { callback ->
            val cluster = getDoorLockClusterForDevice(connectedDevicePtr, endpoint)
            if (isLocked) {
                cluster.lockDoor(callback, pinOptional, PIN_TIMEOUT_MS)
            } else {
                cluster.unlockDoor(callback, pinOptional, PIN_TIMEOUT_MS)
            }
        }
    }

    /**
     * Subscribes to the LockState attribute of a door lock endpoint and emits its state as it
     * changes.
     *
     * The subscription reports changes instantly and otherwise sends a heartbeat every 10 seconds;
     * establishing the underlying session is subject to a 10 second timeout. The returned [Flow]
     * closes with an exception if the subscription cannot be established.
     *
     * @param deviceId the commissioned device to observe.
     * @param endpoint the Matter endpoint exposing the Door Lock cluster.
     * @param doorLockClusterId the Door Lock cluster ID reported by this device (typically 257L).
     * @return a cold [Flow] emitting the current [LockDeviceState].
     */
    fun observeLockState(
        deviceId: DeviceId,
        endpoint: Int,
        doorLockClusterId: Long,
    ): Flow<LockDeviceState> = callbackFlow {
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
                val rawValue = endpointState.getClusterState(doorLockClusterId)
                    ?.getAttributeState(LOCK_STATE_ATTRIBUTE_ID)?.value as? Number ?: return
                val lockState = LockDeviceState.create(rawValue.toInt())
                NordicLogger.info("Received LockState report: lockState=$lockState", tag = TAG)
                trySend(lockState)
            }
        }

        try {
            val devicePtr = connectedDevicePointer(deviceId)
            chipClient.subscribeAttribute(
                reportCallback = reportCallback,
                devicePtr = devicePtr,
                attributePaths = listOf(
                    ChipAttributePath.newInstance(endpoint, doorLockClusterId, LOCK_STATE_ATTRIBUTE_ID)
                ),
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

    private suspend fun getConnectedDevicePointerOrNull(deviceId: DeviceId): Long? =
        try {
            connectedDevicePointer(deviceId)
        } catch (e: IllegalStateException) {
            NordicLogger.error("Can't get connectedDevicePointer.", e, tag = TAG)
            null
        }

    private suspend fun connectedDevicePointer(deviceId: DeviceId): Long =
        chipClient.getConnectedDevicePointer(deviceId.longValue)

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

    private fun getDoorLockClusterForDevice(
        devicePtr: Long,
        endpoint: Int
    ): ChipClusters.DoorLockCluster {
        return ChipClusters.DoorLockCluster(devicePtr, endpoint)
    }

    companion object {
        private const val LOCK_STATE_ATTRIBUTE_ID = 0L
        private const val PIN_TIMEOUT_MS = 10000

        private val TAG: String
            get() = "MatterDoorLockController"
    }
}