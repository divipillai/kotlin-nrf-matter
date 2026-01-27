package no.nordicsemi.nrf.matter.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import no.nordicsemi.nrf.matter.datasource.DeviceStateDataSource
import no.nordicsemi.nrf.matter.model.DevicesState
import no.nordicsemi.nrf.matter.serializer.DevicesStateOkioSerializer
import okio.FileSystem
import okio.Path.Companion.toPath

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

internal class IosDevicesStateDataSource(
) : DeviceStateDataSource {

    private val dataStore: DataStore<DevicesState> = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = DevicesStateOkioSerializer,
            producePath = {
                val path = "${getDocumentDirectory()}/devices_state_store.json"
                path.toPath()
            }
        ))

    override val devicesFlow: Flow<DevicesState> = dataStore.data
        .catch { e ->
            if (e is Exception) {
                emit(DevicesState())
            } else {
                throw e
            }
        }

    override suspend fun update(transform: (DevicesState) -> DevicesState) {
        try {
            dataStore.updateData(transform)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun removeDevice(deviceId: Long) {
        dataStore.updateData { current ->
            current.copy(
                devicesStateList = current.devicesStateList
                    .filterNot { it.deviceId == deviceId }
            )
        }
    }
}
