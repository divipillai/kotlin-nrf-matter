package no.nordicsemi.nrf.matter.repository

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import no.nordicsemi.nrf.matter.data.Device
import no.nordicsemi.nrf.matter.data.DeviceType
import no.nordicsemi.nrf.matter.data.Devices

class DevicesRepository {

    private val _devicesFlow = MutableStateFlow(Devices())
    val devicesFlow = _devicesFlow.asStateFlow()

    // Safely update Devices atomically
    private inline fun updateDevices(transform: (Devices) -> Devices) {
        _devicesFlow.update(transform)
    }

    fun incrementAndReturnLastDeviceId(): Long {
        val newId = _devicesFlow.value.lastDeviceId + 1
        updateDevices { it.copy(lastDeviceId = newId) }
        return newId
    }

    fun addDevice(device: Device) {
        updateDevices { devices ->
            devices.copy(devices = devices.devices + device)
        }
    }

    suspend fun updateDevice(device: Device) {
        val index = getIndex(device.deviceId)
        if (index == -1) return

        updateDevices { devices ->
            devices.copy(
                devices = devices.devices.toMutableList().apply {
                    this[index] = device
                }
            )
        }
    }

    suspend fun updateDeviceType(deviceId: Long, deviceType: DeviceType) {
        val index = getIndex(deviceId)
        if (index == -1) return

        updateDevices { devices ->
            val updated = devices.devices[index].copy(deviceType = deviceType)
            devices.copy(
                devices = devices.devices.toMutableList().apply {
                    this[index] = updated
                }
            )
        }
    }

    suspend fun removeDevice(deviceId: Long) {
        val index = getIndex(deviceId)
        if (index == -1) return

        updateDevices { devices ->
            devices.copy(
                devices = devices.devices.toMutableList().apply {
                    removeAt(index)
                }
            )
        }
    }

    suspend fun getLastDeviceId(): Long {
        return devicesFlow.first().lastDeviceId
    }

    suspend fun getDevice(deviceId: Long): Device {
        val devices = devicesFlow.first()
        val index = getIndex(devices, deviceId)
        if (index == -1) throw Exception("Device not found: $deviceId")
        return devices.devices[index]
    }

    suspend fun getAllDevices(): Devices {
        return devicesFlow.first()
    }

    fun clearAllData() {
        updateDevices { Devices() }
    }

    private suspend fun getIndex(deviceId: Long): Int {
        return getIndex(devicesFlow.first(), deviceId)
    }

    private fun getIndex(devices: Devices, deviceId: Long): Int {
        return devices.devices.indexOfFirst { it.deviceId == deviceId }
    }
}
