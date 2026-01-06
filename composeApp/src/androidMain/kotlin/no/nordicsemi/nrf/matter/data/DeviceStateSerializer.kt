package no.nordicsemi.nrf.matter.data

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
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.ExperimentalTime

object DevicesStateJsonSerializer : Serializer<DevicesState> {

    override val defaultValue: DevicesState = DevicesState()

    override suspend fun readFrom(input: InputStream): DevicesState {
        return try {
            val text = input.readBytes().decodeToString()
            if (text.isBlank()) defaultValue
            else Json.decodeFromString(text)
        } catch (e: Exception) {
            throw CorruptionException("Cannot read Devices JSON.", e)
        }
    }

    override suspend fun writeTo(
        t: DevicesState,
        output: OutputStream
    ) {
        val text = Json.encodeToString(t)
        output.write(text.encodeToByteArray())
    }
}

/**
 * Info about the dynamic state of a Matter device that is persisted in a DataStore.
 */
@Serializable
data class DeviceState @OptIn(ExperimentalTime::class) constructor(
    /** Timestamp when the state was captured. */
    val dateCaptured: kotlin.time.Instant,

    /** Device ID within the app's fabric. */
    val deviceId: Long,

    /** Whether the device is offline (false) or online (true). */
    val online: Boolean,

    /**
     * Whether the device is off (false) or on (true).
     * Value should be disregarded if the device is offline.
     */
    val on: Boolean
)


@Serializable
data class DevicesState(
    val devicesStateList: List<DeviceState> = emptyList()
)

/**
 * DataStore to persist the dynamic state of a Matter device.
 *
 */
val Context.devicesStateDataStore: DataStore<DevicesState> by dataStore(
    fileName = "devices_state_store.json",
    serializer = DevicesStateJsonSerializer
)