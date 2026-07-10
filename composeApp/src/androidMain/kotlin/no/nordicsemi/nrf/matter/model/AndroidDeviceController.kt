package no.nordicsemi.nrf.matter.model

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.chip.BindingManager
import no.nordicsemi.nrf.matter.chip.ChipClient
import no.nordicsemi.nrf.matter.chip.MatterDoorLockController
import no.nordicsemi.nrf.matter.chip.MatterLightController
import no.nordicsemi.nrf.matter.chip.MatterManufacturerSpecificController

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

class AndroidDeviceController(
    private val chipClient: ChipClient,
    private val bindingManager: BindingManager,
    private val lightController: MatterLightController,
    private val lockController: MatterDoorLockController,
    private val manufacturerSpecificController: MatterManufacturerSpecificController,
) : DeviceController {

    override val bindingLogs: Flow<String>
        get() = chipClient.chipLogFlow

    override suspend fun setDeviceOnOff(
        deviceId: DeviceId,
        isDeviceOnline: Boolean,
        isOn: Boolean,
        endpoint: Int,
    ) {
        lightController.setOnOffDeviceStateOnOffCluster(
            deviceId = deviceId,
            isOn = isOn,
            endpoint = endpoint
        )
    }

    override suspend fun setLed(
        deviceId: DeviceId,
        isOn: Boolean,
        endpoint: Int
    ) {
        manufacturerSpecificController.setLed(
            deviceId = deviceId,
        )
    }

    override suspend fun unlinkDevice(deviceId: DeviceId) {
        chipClient.decommissionDevice(deviceId.longValue)
    }

    override suspend fun lockUnlockDoor(
        deviceId: DeviceId,
        isLocked: Boolean,
        endpoint: Int
    ) {
        lockController.lockUnlockDoor(
            deviceId = deviceId,
            isLocked = isLocked,
            endpoint = endpoint,
        )
    }

    override suspend fun bind(
        sourceNodeId: DeviceId,
        sourceEndpoint: Int,
        targetNodeId: DeviceId,
        targetEndpoint: Int,
        clusterId: Long
    ) {
        bindingManager.createBinding(
            switchNodeId = sourceNodeId.longValue,
            switchEndpoint = sourceEndpoint,
            lightNodeId = targetNodeId.longValue,
            lightEndpoint = targetEndpoint,
            clusterId = clusterId
        )

    }

    override fun observeButtonChanges(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<Boolean> {
        return manufacturerSpecificController.observeButtonChanges(
            deviceId = deviceId,
            endpoint = endpoint,
        )
    }

    override suspend fun generateRandomNumber(
        deviceId: DeviceId,
        endpoint: Int
    ): Int {
        return manufacturerSpecificController.generateRandomNumber(deviceId)?.toInt() ?: -1
    }

    override suspend fun setBrightnessLevel(
        deviceId: DeviceId,
        brightnessLevel: Int,
        endpoint: Int
    ) {
        lightController.setBrightnessLevel(
            deviceId = deviceId,
            brightnessLevel = brightnessLevel,
            endpoint = endpoint
        )
    }

    override fun observeLightDeviceState(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<Boolean> {
        return lightController.observeLightState(
            deviceId = deviceId,
            endpoint = endpoint
        )
    }

    override fun observeBrightnessState(
        deviceId: DeviceId,
        endpoint: Int
    ): Flow<Float> {
        return lightController.observeBrightnessState(
            deviceId = deviceId,
            endpoint = endpoint
        )
    }

    override fun observeLockDeviceState(
        deviceId: DeviceId,
        endpoint: Int,
        doorLockClusterId: Long
    ): Flow<LockDeviceState> {
        return lockController.observeLockState(
            deviceId = deviceId,
            endpoint = endpoint,
            doorLockClusterId = doorLockClusterId
        )
    }
}
