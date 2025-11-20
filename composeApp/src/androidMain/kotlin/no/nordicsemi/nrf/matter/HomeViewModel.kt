package no.nordicsemi.nrf.matter

import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import com.google.android.gms.home.matter.commissioning.CommissioningResult

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
class HomeViewModel(
//    private val devicesRepository: DevicesRepository,
) : ViewModel() {
//     val devices = devicesRepository.devicesFlow
    // Saves the result of the GPS Commissioning action (step 4).
    // It is then used in step 5 to complete the commissioning.
    private var gpsCommissioningResult: CommissioningResult? = null


    // This is step 4 of the commissioning flow where GPS takes over.
    // We save the result we get from GPS, which will be used by commissionedDeviceNameCaptured
    // after the device name is captured.
    fun gpsCommissioningDeviceSucceeded(activityResult: ActivityResult) {
        gpsCommissioningResult =
            CommissioningResult.fromIntentSenderResult(activityResult.resultCode, activityResult.data)
        Log.i("AAA",
            "Device commissioned successfully! deviceName [${gpsCommissioningResult!!.deviceName}]"
        )
        Log.i("AAA","Device commissioned successfully! room [${gpsCommissioningResult!!.room}]")
        Log.i("AAA",
            "Device commissioned successfully! DeviceDescriptor of device:\n" +
                    "productId [${gpsCommissioningResult!!.commissionedDeviceDescriptor.productId}]\n" +
                    "vendorId [${gpsCommissioningResult!!.commissionedDeviceDescriptor.vendorId}]\n" +
                    "hashCode [${gpsCommissioningResult!!.commissionedDeviceDescriptor.hashCode()}]"
        )
    }

    // Called in Step 5 of the Device Commissioning flow when the GPS activity for
    // commissioning the device has failed.
    fun commissionDeviceFailed(resultCode: Int) {
        if (resultCode == 0) {
            // User simply wilfully exited from GPS commissioning.
            return
        }
        val title = "Commissioning the device failed"
        Log.e("AAA",title)
        Log.d("AAA", "commissionDeviceFailed: $title, $resultCode")
    }


}