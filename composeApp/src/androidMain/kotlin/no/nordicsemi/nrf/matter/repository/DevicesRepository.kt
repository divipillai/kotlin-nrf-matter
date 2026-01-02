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
import android.content.Context
import android.util.Log
import androidx.datastore.core.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import no.nordicsemi.nrf.matter.data.Device
import no.nordicsemi.nrf.matter.data.DeviceType
import no.nordicsemi.nrf.matter.data.Devices
import no.nordicsemi.nrf.matter.data.devicesDataStore

class DevicesRepository(
    context: Context
) {

    // The datastore managed by DevicesRepository.
    private val devicesDataStore = context.devicesDataStore

    // The Flow to read data from the DataStore.
    val devicesFlow: Flow<Devices> =
        devicesDataStore.data.catch { exception ->
            if (exception is IOException) {
                Log.e("AAA", "Error reading devices with exception", exception)
                emit(Devices())
            } else {
                throw exception
            }
        }

    suspend fun incrementAndReturnLastDeviceId(): Long {
        val updatedDevices = devicesDataStore.updateData { devices ->
            val newLastDeviceId = devices.lastDeviceId + 1
            Log.d("AAA", "incrementAndReturnLastDeviceId(): newLastDeviceId [$newLastDeviceId]")
            devices.copy(lastDeviceId = newLastDeviceId)
        }
        return updatedDevices.lastDeviceId
    }

    suspend fun addDevice(device: Device) {
        Log.d("AAA", "addDevice: device [$device]")
        devicesDataStore.updateData { devices ->
            devices.copy(devices = devices.devices + device)
        }
    }

    suspend fun updateDevice(device: Device) {
        Log.d("AAA", "updateDevice: device [$device]")
        devicesDataStore.updateData { devices ->
            val updatedDevices = devices.devices.map {
                if (it.deviceId == device.deviceId) device else it
            }
            devices.copy(devices = updatedDevices)
        }
    }

    suspend fun updateDeviceType(deviceId: Long, deviceType: DeviceType) {
        Log.d("AAA", "updateDeviceType: deviceId [$deviceId] deviceType [$deviceType]")

        var wasUpdated = false

        devicesDataStore.updateData { devices ->
            val updatedDevices = devices.devices.map { device ->
                if (device.deviceId == deviceId) {
                    wasUpdated = true
                    device.copy(deviceType = deviceType)
                } else {
                    device
                }
            }
            devices.copy(devices = updatedDevices)
        }

        if (!wasUpdated) {
            Log.e(
                "AAA",
                "Unable to get device information to update its type: deviceId [$deviceId] deviceType [$deviceType]"
            )
        }
    }

    suspend fun removeDevice(deviceId: Long) {
        Log.d("AAA", "removeDevice: device [$deviceId]")

        var removed = false

        devicesDataStore.updateData { devices ->
            val updatedDevices = devices.devices.filterNot {
                if (it.deviceId == deviceId) {
                    removed = true
                    true
                } else {
                    false
                }
            }
            devices.copy(devices = updatedDevices)
        }

        if (!removed) {
            throw Exception("Device not found: $deviceId")
        }
    }

    suspend fun getLastDeviceId(): Long {
        return devicesFlow.first().lastDeviceId
    }

    suspend fun getDevice(deviceId: Long): Device {
        return devicesFlow.first().devices.firstOrNull { it.deviceId == deviceId }
            ?: throw Exception("Device not found: $deviceId")
    }

    suspend fun getAllDevices(): Devices {
        return devicesFlow.first()
    }

    suspend fun clearAllData() {
        devicesDataStore.updateData {
            Devices()
        }
    }

    // ---------- Helpers (JSON-friendly) ----------

    private suspend fun getIndex(deviceId: Long): Int {
        return devicesFlow.first().devices.indexOfFirst { it.deviceId == deviceId }
    }

    private fun getIndex(devices: Devices, deviceId: Long): Int {
        return devices.devices.indexOfFirst { it.deviceId == deviceId }
    }

    private suspend fun getIndexAndDevice(deviceId: Long): Pair<Int?, Device?> {
        val devices = devicesFlow.first()
        return getIndexAndDevice(devices, deviceId)
    }

    private fun getIndexAndDevice(devices: Devices, deviceId: Long): Pair<Int?, Device?> {
        val index = devices.devices.indexOfFirst { it.deviceId == deviceId }
        return if (index >= 0) index to devices.devices[index] else null to null
    }
}

