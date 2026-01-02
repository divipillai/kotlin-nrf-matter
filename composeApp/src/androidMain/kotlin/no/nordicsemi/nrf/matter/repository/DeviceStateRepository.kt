package no.nordicsemi.nrf.matter.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import no.nordicsemi.nrf.matter.data.DeviceState
import no.nordicsemi.nrf.matter.data.DevicesState
import no.nordicsemi.nrf.matter.data.devicesStateDataStore
import java.io.IOException
import java.time.Instant

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
 * Singleton repository that updates the dynamic state of the devices on the homesampleapp fabric.
 */
class DevicesStateRepository(context: Context) {

    // The datastore managed by DevicesStateRepository.
    private val devicesStateDataStore = context.devicesStateDataStore

    // The Flow to read data from the DataStore.
    val devicesStateFlow =
        devicesStateDataStore.data.catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e("AAA", "Error reading devicesState.$exception")
                emit(DevicesState())
            } else {
                throw exception
            }
        }

    /** The latest device state update */
    private val _lastUpdatedDeviceState = MutableLiveData(DevicesState())
    val lastUpdatedDeviceState: LiveData<DevicesState>
        get() = _lastUpdatedDeviceState

    /** Add Device State to the datastore */
    suspend fun addDeviceState(
        deviceId: Long,
        isOnline: Boolean,
        isOn: Boolean
    ) {
        val updatedState = devicesStateDataStore.updateData { currentState ->

            val updatedDevices = currentState.devicesStateList
                .filterNot { it.deviceId == deviceId } + // remove old entry if exists
                    DeviceState(
                        dateCaptured = Instant.now(),
                        deviceId = deviceId,
                        online = isOnline,
                        on = isOn
                    )

            currentState.copy(devicesStateList = updatedDevices)
        }

        _lastUpdatedDeviceState.postValue(updatedState)
    }


    suspend fun updateDeviceState(
        deviceId: Long,
        isOnline: Boolean,
        isOn: Boolean
    ) {
        var wasUpdated = false

        val updatedDevicesState = devicesStateDataStore.updateData { currentState ->

            val updatedDevices = currentState.devicesStateList.map { deviceState ->
                if (deviceState.deviceId == deviceId) {
                    wasUpdated = true
                    deviceState.copy(
                        dateCaptured = Instant.now(),
                        online = isOnline,
                        on = isOn
                    )
                } else {
                    deviceState
                }
            }

            currentState.copy(devicesStateList = updatedDevices)
        }

        if (wasUpdated) {
            _lastUpdatedDeviceState.postValue(updatedDevicesState)
        } else {
            Log.w(
                "AAA",
                "We did not find device [$deviceId] in devicesStateRepository; it should have been there???"
            )
            addDeviceState(deviceId, isOnline = isOnline, isOn = isOn)
        }
    }

    suspend fun loadDeviceState(deviceId: Long): DeviceState? {
        return devicesStateFlow
            .first()
            .devicesStateList
            .firstOrNull { it.deviceId == deviceId }
    }


    suspend fun getAllDevicesState(): DevicesState {
        return devicesStateFlow.first()
    }

    suspend fun clearAllData() {
        devicesStateDataStore.updateData {
            DevicesState()
        }
    }

}