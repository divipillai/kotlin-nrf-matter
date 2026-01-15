package no.nordicsemi.nrf.matter.home

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.home.matter.commissioning.CommissioningResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.chip.ChipClient
import no.nordicsemi.nrf.matter.chip.ClustersHelper
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.Devices
import no.nordicsemi.nrf.matter.model.DevicesState
import no.nordicsemi.nrf.matter.model.UserPreferences
import no.nordicsemi.nrf.matter.repository.DevicesRepository
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.repository.UserPreferencesRepository

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
 * Encapsulates all of the information on a specific device. Note that the app currently only
 * supports Matter devices with server attribute "ON/OFF".
 */
data class DeviceUiModel(
    // Device information that is persisted in a Proto DataStore. See DevicesRepository.
    val device: Device,

    // Device state information that is retrieved dynamically.
    // Whether the device is online or offline.
    val isOnline: Boolean,
    // Whether the device is on or off.
    val isOn: Boolean,
)

/**
 * UI model that encapsulates the information about the devices to be displayed on the Home screen.
 */
data class DevicesListUiModel(
    // The list of devices.
    val devices: List<DeviceUiModel>,

    // Whether offline devices should be shown.
    val showOfflineDevices: Boolean,
)

class HomeViewModel(
    context: Context,
    private val devicesRepository: DevicesRepository,
    private val devicesStateRepository: DevicesStateRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private var gpsCommissioningResult: CommissioningResult? = null
    val chipsClient: ChipClient = ChipClient(context)
    val clustersHelper: ClustersHelper = ClustersHelper(chipsClient)

    private val devicesFlow = devicesRepository.devicesFlow
    private val devicesStateFlow = devicesStateRepository.devicesStateFlow
    private val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    // Controls whether the "New Device" AlertDialog should be shown in the UI.
    private var _showNewDeviceNameAlertDialog = MutableStateFlow(false)
    val showNewDeviceNameAlertDialog: StateFlow<Boolean> =
        _showNewDeviceNameAlertDialog.asStateFlow()

    init {
        liveData { emit(devicesRepository.getAllDevices()) }
        liveData { emit(devicesStateRepository.getAllDevicesState()) }
        liveData { emit(userPreferencesRepository.getData()) }
    }

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
        // Now we need to capture the device name.
        _showNewDeviceNameAlertDialog.value = true
        Log.d(
            "AAA",
            "gpsCommissioningDeviceSucceeded: Show device name is ${_showNewDeviceNameAlertDialog.value}"
        )
        // TODO: Add device to the devices repository.
        // TODO: Add device state to repository: isOnline:true isOn:false

        onCommissionedDeviceNameCaptured("Device-Test")
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
        Log.d("AAA", "onCommissionedDeviceNameCaptured: ")
        // Add the device to the devices repository.
        _showNewDeviceNameAlertDialog.value = false
        viewModelScope.launch {
            val deviceId = gpsCommissioningResult?.token?.toLong()!!
            // read device's vendor name and product name
            val vendorName =
                try {
                    clustersHelper.readBasicClusterVendorNameAttribute(deviceId)
                } catch (ex: Exception) {
                    Log.e("AAA", "Failed to read VendorName attribute with exception: $ex")
                    ""
                }

            val productName =
                try {
                    clustersHelper.readBasicClusterProductNameAttribute(deviceId)
                } catch (ex: Exception) {
                    Log.e("AAA", "Failed to read ProductName attribute with exception: $ex")
                    ""
                }

            try {

                Log.d("BBB", "Commissioning: Adding device to repository")
                val deviceType = convertToAppDeviceType(
                    gpsCommissioningResult?.commissionedDeviceDescriptor?.deviceType?.toLong()!!
                )
                val device = Device(
                    vendorName = vendorName,
                    productName = productName,
                    dateCommissioned = gpsCommissioningResult?.token?.toLong(),
                    vendorId = gpsCommissioningResult?.commissionedDeviceDescriptor?.vendorId.toString(),
                    productId = gpsCommissioningResult?.commissionedDeviceDescriptor?.productId.toString(),
                    deviceType = deviceType,
                    deviceId = deviceId,
                    name = gpsCommissioningResult?.deviceName,
                )
                devicesRepository.addDevice(device)
                Log.d("AAA", "Commissioning: Adding device to repository: $device")
//                )
                // Add device state to repository: isOnline:true isOn:false
                devicesStateRepository.addDeviceState(deviceId, isOnline = true, isOn = false)
            } catch (e: Exception) {
                val msg = "Adding device [${deviceId}] [${deviceName}] to app's repository failed."
                Log.e("BBB", "onCommissionedDeviceNameCaptured: $msg, $e")
            }

            // Introspect the device and update its deviceType.
            // TODO: Need to get capabilities information and store that in the devices repository.
            // (e.g on/off on which endpoint).
            val deviceMatterInfoList = clustersHelper.fetchDeviceMatterInfo(deviceId)
            Log.d("BBB", "*** MATTER DEVICE INFO ***")
            var gotDeviceType = false
            deviceMatterInfoList.forEach { deviceMatterInfo ->
                Log.d("AAA", "Processing endpoint [${deviceMatterInfo.endpoint}]")
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
                        // TODO: Handle this properly once we have specific examples to learn from.
                        devicesRepository.updateDeviceType(
                            deviceId,
                            convertToAppDeviceType(deviceMatterInfo.types.first()),
                        )
                        gotDeviceType = true
                    }
                }
            }

            // update device name
            try {
                clustersHelper.writeBasicClusterNodeLabelAttribute(deviceId, deviceName)
            } catch (ex: Exception) {
                Log.e("AAA", "Failed to write NodeLabel", ex)
            }
        }
    }

    fun updateDeviceStateOn(deviceId: Long, isOn: Boolean) {
        viewModelScope.launch {
            try {
                devicesStateRepository.updateDeviceState(
                    deviceId = deviceId,
                    isOnline = true,
                    isOn = isOn
                )

                clustersHelper.setOnOffDeviceStateOnOffCluster(
                    deviceId,
                    isOn,
                    0xd // TODO: This endpoint is hardcoded, replace with the correct endpoint.
                )

            } catch (e: Exception) {
                // Rollback on failure
                devicesStateRepository.updateDeviceState(
                    deviceId = deviceId,
                    isOnline = false,
                    isOn = !isOn
                )
            }
        }
    }

    // Every time the list of devices or user preferences are updated (emit is triggered),
    // we recreate the DevicesListUiModel
    private val devicesListUiModelFlow =
        combine(devicesFlow, devicesStateFlow, userPreferencesFlow) { devices: Devices,
                                                                      devicesStates: DevicesState,
                                                                      userPreferences: UserPreferences ->
            Log.d("AAA", "*** devicesListUiModelFlow changed ***")

            // TODO: Before demo clear the devices from repositories.
//             devicesRepository.clearAllData()
//            devicesStateRepository.clearAllData()


            return@combine DevicesListUiModel(
                devices = processDevices(devices, devicesStates, userPreferences),
                showOfflineDevices = !userPreferences.hideOfflineDevices,
            )
        }
    val devicesUiModelLiveData = devicesListUiModelFlow.asLiveData()

    private fun processDevices(
        devices: Devices,
        devicesStates: DevicesState,
        userPreferences: UserPreferences,
    ): List<DeviceUiModel> {
        val devicesUiModel = ArrayList<DeviceUiModel>()
        devices.devicesList.forEach { device ->
            Log.d("AAA", "processDevices() deviceId: [${device.deviceId}]}")
            val state = devicesStates.devicesStateList.find { it.deviceId == device.deviceId }
            if (userPreferences.hideOfflineDevices) {
                if (state?.online != true) return@forEach
            }
            if (state == null) {
                Log.d("AAA", "    deviceId setting default value for state")
                devicesUiModel.add(DeviceUiModel(device, isOnline = false, isOn = false))
            } else {
                Log.d("AAA", "    deviceId setting its own value for state")
                devicesUiModel.add(DeviceUiModel(device, state.online, state.on))
            }
        }
        return devicesUiModel
    }
}


fun convertToAppDeviceType(matterDeviceType: Long): DeviceType {
    return when (matterDeviceType) {
        256L -> DeviceType.LIGHT_ON_OFF // 0x0100 On/Off Light
        257L -> DeviceType.DIMMABLE_LIGHT // 0x0101 Dimmable Light
        259L -> DeviceType.LIGHT_SWITCH// 0x0103 On/Off Light Switch
        266L -> DeviceType.OUTLET // 0x010A (On/Off Plug-in Unit)
        268L -> DeviceType.COLOR_TEMPERATURE_LIGHT // 0x010C Color Temperature Light
        269L -> DeviceType.EXTENDED_COLOR_LIGHT // 0x010D Extended Color Light
        else -> DeviceType.UNKNOWN
    }
}
