package no.nordicsemi.nrf.matter.domain

import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.repository.DevicesRepository
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository

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
class DeviceCommandHandler(
    private val devicesRepository: DevicesRepository,
    private val devicesStateRepository: DevicesStateRepository,
    private val deviceController: DeviceController,
) {

    suspend fun execute(
        deviceId: Long,
        command: DeviceCommand
    ) {
        val device = devicesRepository.getDeviceOrNull(deviceId) ?: return

        when (command) {

            is DeviceCommand.SetPower -> {
                handlePower(device, deviceId, command.isOn)
            }

            is DeviceCommand.SetLock -> {
                handleLock(device, deviceId, command.locked)
            }

            is DeviceCommand.SetLevel -> {
                handleLevel(device, deviceId, command.level)
            }
        }
    }

    private suspend fun handlePower(
        device: Device,
        deviceId: Long,
        isOn: Boolean
    ) {
        val endpoint = resolveEndpoint(device, clusterId = 6L)

        try {
            devicesStateRepository.updateDeviceState(
                deviceId = deviceId,
                isOnline = true,
                isOn = isOn
            )

            deviceController.setDeviceOnOff(
                deviceId = deviceId,
                isDeviceOnline = true,
                isOn = isOn,
                endpoint = endpoint
            )

        } catch (e: Exception) {

            devicesStateRepository.updateDeviceState(
                deviceId = deviceId,
                isOnline = false,
                isOn = !isOn
            )

            throw e
        }
    }
    private suspend fun handleLock(
        device: Device,
        deviceId: Long,
        isOn: Boolean
    ) {
        // TODO: implement this.
    }
    private suspend fun handleLevel(
        device: Device,
        deviceId: Long,
        level: Int,
    ) {
       // TODO: implement this.
    }

    private fun resolveEndpoint(device: Device, clusterId: Long): Int {
        return device.deviceMatterInfo
            .firstOrNull { it.serverClusters.contains(clusterId) }
            ?.endpoint ?: 0
    }

}
