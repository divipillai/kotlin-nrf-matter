package no.nordicsemi.nrf.matter.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import no.nordicsemi.nrf.matter.model.DeviceState
import no.nordicsemi.nrf.matter.model.DevicesState
import no.nordicsemi.nrf.matter.model.devicesStateDataStore
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
class DevicesStateRepository(private val context: Context) {

    // The datastore managed by DevicesStateRepository.
    private val devicesStateDataStore = context.devicesStateDataStore.data.map {
        Log.d("AAA", "devicesStateDataStore: $it")
        it
    }

    // The Flow to read data from the DataStore.
    val devicesStateFlow =
        devicesStateDataStore.catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e("AAA", "Error reading devicesState.$exception")
                emit(DevicesState())
            } else {
                throw exception
            }
        }

    /** Add Device State to the datastore */
    @OptIn(ExperimentalTime::class)
    suspend fun addDeviceState(
        deviceId: Long,
        isOnline: Boolean,
        isOn: Boolean
    ) {
        context.devicesStateDataStore.updateData { currentState ->
            val updatedDevices = currentState.devicesStateList
                .filterNot { it.deviceId == deviceId } + // remove old entry if exists
                    DeviceState(
                        dateCaptured = Clock.System.now(),
                        deviceId = deviceId,
                        online = isOnline,
                        on = isOn
                    )

            currentState.copy(devicesStateList = updatedDevices)
        }
    }


    @OptIn(ExperimentalTime::class)
    suspend fun updateDeviceState(
        deviceId: Long,
        isOnline: Boolean,
        isOn: Boolean
    ) {
        context.devicesStateDataStore.updateData { currentState ->
            val exists = currentState.devicesStateList.any { it.deviceId == deviceId }

            val updatedList = if (exists) {
                currentState.devicesStateList.map { device ->
                    if (device.deviceId == deviceId) {
                        device.copy(online = isOnline, on = isOn, dateCaptured = Clock.System.now())
                    } else device
                }
            } else {
                currentState.devicesStateList + DeviceState(
                    deviceId = deviceId,
                    online = isOnline,
                    on = isOn,
                    dateCaptured = Clock.System.now()
                )
            }

            currentState.copy(devicesStateList = updatedList)
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
        context.devicesStateDataStore.updateData {
            DevicesState()
        }
    }

}