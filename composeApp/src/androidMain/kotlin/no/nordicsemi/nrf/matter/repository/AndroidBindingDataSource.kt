package no.nordicsemi.nrf.matter.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import no.nordicsemi.nrf.matter.binding.BindingDataSource
import no.nordicsemi.nrf.matter.model.DeviceBinding
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

class AndroidBindingDataSource(
    private val context: Context
): BindingDataSource {

    private val Context.dataStore by preferencesDataStore(name = "bindings")

    private val BINDINGS_KEY = stringPreferencesKey("bindings_json")

    override suspend fun save(binding: DeviceBinding) {
        context.dataStore.edit { prefs ->
            val current = prefs[BINDINGS_KEY]?.let { decode(it) } ?: emptyList()

            val updated = current
                .filterNot { it.id == binding.id } + binding

            prefs[BINDINGS_KEY] = encode(updated)
        }
    }

    override suspend fun getBindingsForDevice(deviceId: DeviceId): List<DeviceBinding> {
        val prefs = context.dataStore.data.first()
        val bindings = prefs[BINDINGS_KEY]?.let { decode(it) } ?: emptyList()

        return bindings.filter {
            it.sourceNodeId == deviceId || it.targetNodeId == deviceId
        }
    }

    private fun encode(list: List<DeviceBinding>): String {
        return Json.encodeToString(list)
    }

    private fun decode(json: String): List<DeviceBinding> {
        return Json.decodeFromString(json)
    }
}