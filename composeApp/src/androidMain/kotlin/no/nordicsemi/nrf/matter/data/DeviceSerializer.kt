package no.nordicsemi.nrf.matter.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.serialization.Serializable
import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

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

object DevicesJsonSerializer: Serializer<Devices> {

    override val defaultValue: Devices = Devices()

     override suspend fun readFrom(input: InputStream): Devices {
        return try {
            val text = input.readBytes().decodeToString()
            if (text.isBlank()) defaultValue
            else Json.decodeFromString(text)
        } catch (e: Exception) {
            throw CorruptionException("Cannot read Devices JSON.", e)
        }
    }

     override suspend fun writeTo(t: Devices, output: OutputStream) {
        val text = Json.encodeToString(t)
        output.write(text.encodeToByteArray())
    }
}

val Context.devicesDataStore: DataStore<Devices> by dataStore(
    fileName = "devices_store.json",
    serializer = DevicesJsonSerializer
)

@Serializable
data class Device(
    val dateCommissioned: Long? = null,
    val vendorId: String? = null,
    val productId: String? = null,
    val deviceType: DeviceType = DeviceType.TYPE_UNSPECIFIED,
    val deviceId: Long = 0L,
    val name: String? = null,
    val room: String? = null,
    val productName: String? = null,
    val vendorName: String? = null
)

@Serializable
enum class DeviceType {
    TYPE_UNSPECIFIED,
    TYPE_UNKNOWN,
    TYPE_LIGHT,
    TYPE_OUTLET,
    TYPE_DIMMABLE_LIGHT,
    TYPE_COLOR_TEMPERATURE_LIGHT,
    TYPE_EXTENDED_COLOR_LIGHT,
    TYPE_LIGHT_SWITCH
}

@Serializable
data class Devices(
    val lastDeviceId: Long = 0L,
    val devices: List<Device> = emptyList()
)


