package no.nordicsemi.nrf.matter.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.home.matter.commissioning.CommissioningCompleteMetadata
import com.google.android.gms.home.matter.commissioning.CommissioningRequestMetadata
import com.google.android.gms.home.matter.commissioning.CommissioningService
import com.google.android.gms.home.matter.commissioning.CommissioningService.CommissioningError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.chip.ChipClient
import no.nordicsemi.nrf.matter.repository.DevicesRepository
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import java.lang.Long.max
import java.security.SecureRandom
import kotlin.math.abs

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

class AppCommissioningService : Service(), CommissioningService.Callback, KoinComponent {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val devicesRepository: DevicesRepository by inject()
    private val chipClient: ChipClient by inject()

    private val devicesStateRepository: DevicesStateRepository by inject()
    private lateinit var commissioningServiceDelegate: CommissioningService

    override fun onCreate() {
        super.onCreate()
        // May be invoked without MainActivity being called to initialize APP_NAME.
        // So do it here as well.
        Log.d("AAA", "onCreate: ")
        commissioningServiceDelegate = CommissioningService.Builder(this).setCallback(this).build()
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("AAA", "onBind(): intent [${intent}]")
        return commissioningServiceDelegate.asBinder()
    }

    override fun onCommissioningRequested(metadata: CommissioningRequestMetadata) {
        Log.d(
            "AAA",
            "*** onCommissioningRequested ***:\n" +
                    "\tdeviceDescriptor: " +
                    "deviceType [${metadata.deviceDescriptor.deviceType}] " +
                    "vendorId [${metadata.deviceDescriptor.vendorId}] " +
                    "productId [${metadata.deviceDescriptor.productId}]\n" +
                    "\tnetworkLocation: " +
                    "IP address toString() [${metadata.networkLocation.ipAddress}] " +
                    "IP address hostAddress [${metadata.networkLocation.ipAddress.hostAddress}] " +
                    "port [${metadata.networkLocation.port}]\n" +
                    "\tpassCode [${metadata.passcode}]"
        )

        // Perform commissioning on custom fabric for the sample app.
        serviceScope.launch {
            val deviceId = devicesRepository.incrementAndReturnLastDeviceId()
            try {
                Log.d(
                    "AAA",
                    "Commissioning: App fabric -> ChipClient.establishPaseConnection(): deviceId [${deviceId}]"
                )
                Log.d(
                    "AAA",
                    "Commissioning Step 1: ChipClient.establishPaseConnection(): deviceId [${deviceId}]"
                )
                chipClient.awaitEstablishPaseConnection(
                    deviceId,
                    metadata.networkLocation.ipAddress.hostAddress!!,
                    metadata.networkLocation.port,
                    metadata.passcode
                )

                Log.d(
                    "AAA",
                    "Commissioning: App fabric -> ChipClient.commissionDevice(): deviceId [${deviceId}]"
                )
                Log.d(
                    "AAA",
                    "Commissioning Step 2: ChipClient.commissionDevice(): deviceId [${deviceId}]"
                )

                chipClient.awaitCommissionDevice(deviceId, null)

                Log.d(
                    "AAA","Commissioning Step 3: Adding device to repository")
                devicesStateRepository.addDeviceState(
                    deviceId,
                    isOnline = true,
                    isOn = false
                )
                Log.d(
                    "AAA",
                    "Commissioning Step 5: Calling commissioningServiceDelegate.sendCommissioningComplete()")
                commissioningServiceDelegate
                    .sendCommissioningComplete( CommissioningCompleteMetadata.builder().setToken(deviceId.toString()).build())
                    .addOnSuccessListener {
                        Log.d(
                            "AAA",
                            "Commissioning: commissioningServiceDelegate.sendCommissioningError() succeeded"
                        )
                    }
                    .addOnFailureListener { e2 ->
                        Log.e(
                            "AAA",
                            "Commissioning: commissioningServiceDelegate.sendCommissioningError() failed",
                            e2,
                        )
                    }

            } catch (e: Exception) {
                Log.e("AAA", "onCommissioningRequested() failed with exception: $e")
                // No way to determine whether this was ATTESTATION_FAILED or DEVICE_UNREACHABLE.
                commissioningServiceDelegate
                    .sendCommissioningError(CommissioningError.OTHER)
                    .addOnFailureListener { e2 ->
                        Log.e(
                            "AAA",
                            "Commissioning: commissioningServiceDelegate.sendCommissioningError() failed",
                            e2,
                        )
                    }
                return@launch
            }

//            Log.d(
//                "AAA",
//                "Commissioning: Calling commissioningServiceDelegate.sendCommissioningComplete()"
//            )
//            commissioningServiceDelegate
//                .sendCommissioningComplete(
//                    CommissioningCompleteMetadata.builder().setToken(deviceId.toString()).build()
//                )
//                .addOnSuccessListener {
//                    Log.d(
//                        "AAA",
//                        "Commissioning: commissioningServiceDelegate.sendCommissioningComplete() succeeded"
//                    )
//                }
//                .addOnFailureListener { e ->
//                    Log.e(
//                        "AAA",
//                        "Commissioning: commissioningServiceDelegate.sendCommissioningComplete() failed",
//                        e
//                    )
//                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AAA", "onStartCommand(): intent [${intent}] flags [${flags}] startId [${startId}]")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AAA", "onDestroy()")
        serviceJob.cancel()
    }

    /**
     * Generates the device id for the device being commissioned ToDo() move this function into an
     * appropriate class to make it visible in HomeFragmentRecyclerViewTest
     *
     * @param generator the method used to generate the device id
     */
    private suspend fun getNextDeviceId(generator: DeviceIdGenerator): Long {
        return when (generator) {
            DeviceIdGenerator.Incremental -> {
                devicesRepository.incrementAndReturnLastDeviceId()
            }

            DeviceIdGenerator.Random -> {
                generateNextDeviceId()
            }
        }
    }

}

enum class DeviceIdGenerator {
    Random,
    Incremental
}

/** Generates a random number to be used as a device identifier during device commissioning */
fun generateNextDeviceId(): Long {
    val secureRandom =
        try {
            SecureRandom.getInstance("SHA1PRNG")
        } catch (ex: Exception) {
//            Timber.w(ex, "Failed to instantiate SecureRandom with SHA1PRNG")
            // instantiate with the default algorithm
            SecureRandom()
        }

    return max(abs(secureRandom.nextLong()), 1)
}