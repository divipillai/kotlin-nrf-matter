package no.nordicsemi.nrf.matter.beacon

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
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.os.ParcelUuid
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.nrf.matter.MatterBeacon
import no.nordicsemi.nrf.matter.MatterBeaconProducer
import no.nordicsemi.nrf.matter.Transport
import java.util.concurrent.ConcurrentHashMap

/** [MatterBeaconProducer] which emits Bluetooth LE beacons as they are discovered. */
class MatterBeaconProducerBle(
    private val bluetoothLeScanner: BluetoothLeScanner?,
    private val context: Context,
) : MatterBeaconProducer {

    @SuppressLint("MissingPermission")
    override fun getBeaconsFlow(): Flow<MatterBeacon> = callbackFlow {
        val beaconEmittedTime = ConcurrentHashMap<MatterBeacon, Long>()

        val scanCallback =
            object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    result.toMatterBeaconOrNull()?.let { beacon ->
                        val currentTime = SystemClock.elapsedRealtime()
                        val shouldWeEmitItAgain =
                            currentTime - (beaconEmittedTime[beacon]
                                ?: 0) > BEACON_EMITTING_DEBOUNCE_IN_MS
                        if (shouldWeEmitItAgain) {
                            beaconEmittedTime[beacon] = currentTime
                            Napier.d("AAA, Emitting BLE beacon [${beacon}]")
                            trySend(beacon)
                        }
                    }
                }
            }

        if (bluetoothLeScanner != null) {
            Napier.d("AAA, Starting BLE scan.")
            bluetoothLeScanner.startScan(
                listOf(
                    ScanFilter.Builder()
                        .setServiceData(MATTER_UUID, byteArrayOf(0), byteArrayOf(0))
                        .build()
                ),
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .build(),
                scanCallback,
            )
        } else {
            Napier.d("AAA, BLE Scanner not available.")
        }

        awaitClose {
            if (bluetoothLeScanner == null) {
                Napier.d("AAA, BLE Scanner not available.")
                return@awaitClose
            }

            val bluetoothAdapter: BluetoothAdapter =
                (context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
            if (bluetoothAdapter.state == BluetoothAdapter.STATE_ON) {
                Napier.d("AAA, Stopping BLE scan.")
                bluetoothLeScanner.stopScan(scanCallback)
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Utility functions

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun ScanResult.toMatterBeaconOrNull(): MatterBeacon? {
        val data = scanRecord?.bytes ?: return null
        // Full record must be at least 14 bytes.
        if (data.size < 14) {
            Napier.d("AAA, Dropping BLE ad with record length %d (expected 14) ${data.size}")
            return null
        }

        // Data payload length is byte 4 and should be exactly 10.
        val dataLength = data[3].toInt()
        if (dataLength < 10) {
            Napier.w("AAA, Dropping BLE ad with data length [${dataLength}] (expected >= 10)")
            return null
        }

        return MatterBeacon(
            name = device.name,
            address = device.address,
            vendorId = ((data[10].toInt() or (data[11].toInt() shl 8)) and 0xFFFF),
            productId = ((data[12].toInt() or (data[13].toInt() shl 8)) and 0xFFFF),
            discriminator = ((data[8].toInt() or (data[9].toInt() shl 8)) and 0xFFF),
            Transport.Ble(device.address)
        )
    }

    // ---------------------------------------------------------------------------
    // Companion

    companion object {
        private val MATTER_UUID = ParcelUuid.fromString("0000FFF6-0000-1000-8000-00805F9B34FB")
        private const val BEACON_EMITTING_DEBOUNCE_IN_MS = 1000
    }
}