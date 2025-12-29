package no.nordicsemi.nrf.matter

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.home.matter.commissioning.CommissioningResult
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.chip.ChipClient
import no.nordicsemi.nrf.matter.chip.ClustersHelper
import no.nordicsemi.nrf.matter.data.Device

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
    context: Context,
) : ViewModel() {
    private var gpsCommissioningResult: CommissioningResult? = null
    val chipsClient: ChipClient = ChipClient(context)
    val clustersHelper: ClustersHelper = ClustersHelper(chipsClient)

    fun gpsCommissioningDeviceSucceeded(activityResult: ActivityResult) {
        gpsCommissioningResult =
            CommissioningResult.fromIntentSenderResult(
                activityResult.resultCode,
                activityResult.data
            )
        Log.i(
            "AAA",
            "Device commissioned successfully! deviceName [${gpsCommissioningResult!!.deviceName}]"
        )
        Log.i(
            "AAA",
            "Device commissioned successfully! DeviceDescriptor of device:\n" +
                    "productId [${gpsCommissioningResult!!.commissionedDeviceDescriptor.productId}]\n" +
                    "vendorId [${gpsCommissioningResult!!.commissionedDeviceDescriptor.vendorId}]\n" +
                    "hashCode [${gpsCommissioningResult!!.commissionedDeviceDescriptor.hashCode()}]"
        )
        // TODO: Add device to the devices repository.
        // TODO: Add device state to repository: isOnline:true isOn:false
    }

    fun commissionDeviceFailed(resultCode: Int) {
        if (resultCode == 0) {
            // User simply wilfully exited from GPS commissioning.
            return
        }
        val title = "Commissioning the device failed"
        Log.e("AAA", title)
        Log.d("AAA", "commissionDeviceFailed: $title, $resultCode")
    }

    // Called when the device name has been captured in the UI.
    // This follows a successful gps commissioning (see gpsCommissioningDeviceSucceeded)
    fun onCommissionedDeviceNameCaptured(deviceName: String) {
        // Add the device to the devices repository.
        viewModelScope.launch {
            val deviceId = gpsCommissioningResult?.token?.toLong()!!
            // todo: read device's vendor name and product name

            try {

                Log.d("BBB", "Commissioning: Adding device to repository")
//                devicesRepository.addDevice(
                val device = Device(
                    dateCommissioned = gpsCommissioningResult?.token?.toLong(),
                    vendorId = gpsCommissioningResult?.commissionedDeviceDescriptor?.vendorId.toString(),
                    productId = gpsCommissioningResult?.commissionedDeviceDescriptor?.productId.toString(),
//                        deviceType = gpsCommissioningResult?.commissionedDeviceDescriptor?.deviceType,
                    deviceId = deviceId,
                    name = gpsCommissioningResult?.deviceName,

                    )
                Log.d("AAA", "Commissioning: Adding device to repository: $device")
//                )
                // TODO: Add device state to repository: isOnline:true isOn:false
            } catch (e: Exception) {
                val msg = "Adding device [${deviceId}] [${deviceName}] to app's repository failed."
                Log.e("BBB", "onCommissionedDeviceNameCaptured: $msg, $e")
            }

            // Introspect the device and update its deviceType.
            // TODO: Need to get capabilities information and store that in the devices repository.
            // (e.g on/off on which endpoint).
            val deviceMatterInfoList = clustersHelper.fetchDeviceMatterInfo(deviceId)
            Log.d("BBB", "*** MATTER DEVICE INFO ***")
            val gotDeviceType = false
            deviceMatterInfoList.forEach { deviceMatterInfo ->
                Log.d("AAA", "Processing endpoint [$deviceMatterInfo.endpoint]")
                // Endpoint 0 is the Root Node, so we disregard it.
                if (deviceMatterInfo.endpoint != 0) {
                    if (gotDeviceType) {
                        // TODO: Handle this properly once we have specific examples to learn from.
                        Log.w(
                            "AAA",
                            "The device has more than one endpoint. We're simply using the first one to define the device type."
                        )
                        return@forEach
                    }
                    if (deviceMatterInfo.types.size > 1) {
                        // TODO: Handle this properly once we have specific examples to learn from.
                        Log.w(
                            "AAA",
                            "The endpoint has more than one type. We're simply using the first one to define the device type."
                        )
                    }
                    // TODO: Handle this properly once we have specific examples to learn from.
//                    devicesRepository.updateDeviceType(
//                        deviceId,
//                        convertToAppDeviceType(deviceMatterInfo.types.first()),
//                    )
//                    gotDeviceType = true
                }
            }

            // update device name
            try {
                clustersHelper.writeBasicClusterNodeLabelAttribute(deviceId, deviceName)
            } catch (ex: Exception) {
                val title = "Failed to write NodeLabel"
                Log.e("AAA", title, ex)
            }
        }
    }
}