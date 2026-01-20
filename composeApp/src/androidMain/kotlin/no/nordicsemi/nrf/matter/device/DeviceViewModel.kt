package no.nordicsemi.nrf.matter.device

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.chip.ChipClient
import no.nordicsemi.nrf.matter.chip.ClustersHelper
import no.nordicsemi.nrf.matter.home.DeviceUiModel
import no.nordicsemi.nrf.matter.repository.DevicesRepository
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository

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
sealed interface RemoveDeviceState {
    object Idle : RemoveDeviceState
    object Removing : RemoveDeviceState
    data object ConfirmRemove : RemoveDeviceState

    data class Removed(
        val deviceId: Long,
    ) : RemoveDeviceState

    data class ForceRemove(
        val deviceId: Long,
    ) : RemoveDeviceState
}

data class DeviceUiState(
    val deviceUiModel: DeviceUiModel? = null,
    val removeDeviceState: RemoveDeviceState = RemoveDeviceState.Idle,
)

class DeviceViewModel(
    private val devicesRepository: DevicesRepository,
    private val devicesStateRepository: DevicesStateRepository,
    private val chipClient: ChipClient,
) : ViewModel() {
    val clustersHelper: ClustersHelper = ClustersHelper(chipClient)

    // The UI model for device shown on the Device screen.
    private val _deviceUiState = MutableStateFlow(DeviceUiState())
    val deviceUiState: StateFlow<DeviceUiState> = _deviceUiState.asStateFlow()

    val lastUpdatedDeviceState = devicesStateRepository.lastUpdatedDeviceState


    // Controls whether the "Remove Device" AlertDialog should be shown in the UI.
    private var _showRemoveDeviceAlertDialog = MutableStateFlow(false)
    val showRemoveDeviceAlertDialog: StateFlow<Boolean> = _showRemoveDeviceAlertDialog.asStateFlow()

    // Controls whether a periodic ping to the device is enabled or not.
    private var devicePeriodicPingEnabled: Boolean = true


    // Communicates to the UI that removal of the device has completed successfully.
    // See resetDeviceRemovalCompleted() to reset this state after being handled by the UI.
    private var _deviceRemovalCompleted = MutableStateFlow(false)
    val deviceRemovalCompleted: StateFlow<Boolean> = _deviceRemovalCompleted.asStateFlow()

    // Communicates to the UI that the pairing window is open for device sharing.
    // See resetPairingWindowOpenForDeviceSharing() to reset this state after being handled by the UI.
    private var _pairingWindowOpenForDeviceSharing = MutableStateFlow(false)
    val pairingWindowOpenForDeviceSharing: StateFlow<Boolean> =
        _pairingWindowOpenForDeviceSharing.asStateFlow()

    // -----------------------------------------------------------------------------------------------
    // Load device

    fun loadDevice(deviceId: Long) {
        if (deviceId == _deviceUiState.value.deviceUiModel?.device?.deviceId) {
            return
        } else {
            viewModelScope.launch {
                val device = devicesRepository.getDevice(deviceId)
                val deviceState = devicesStateRepository.loadDeviceState(deviceId)
                var isOnline = false
                var isOn = false
                if (deviceState != null) {
                    isOnline = deviceState.online
                    isOn = deviceState.on
                }
                _deviceUiState.update {
                    it.copy(
                        deviceUiModel = DeviceUiModel(device, isOnline, isOn),
                        removeDeviceState = RemoveDeviceState.Idle
                    )
                }
            }
        }
    }

    // -----------------------------------------------------------------------------------------------
    // Share Device (aka Multi-Admin)

    // TODO: Implement multi-admin.


    // -----------------------------------------------------------------------------------------------
    // Remove device

    // Removes the device. First we remove the fabric from the device, and then we remove the
    // device from the app's devices repository.
    // Note that unlinking the device may take a while if the device is offline. Because of that,
    // a MsgAlertDIalog is shown, without any confirm button, to let the user know that unlinking
    // may take a while. That way the user is not left hanging wondering what is going on.
    // If removing the fabric from the device fails (e.g. device is offline), then another dialog
    // is shown so the user has the option to force remove the device without unlinking
    // the fabric at the device. If a forced removal is selected, then function
    // removeDeviceWithoutUnlink is called.
    // TODO: The device will still be linked to the local Android fabric. We should remove all the
    // fabrics at the device.
    fun removeDevice(deviceId: Long) {
        _deviceUiState.update {
            it.copy(removeDeviceState = RemoveDeviceState.Removing)
        }
        viewModelScope.launch {
            try {
                chipClient.awaitUnpairDevice(deviceId)
            } catch (e: Exception) {
                Log.e("AAA", "Unlinking the device failed with exception: [${e.message}]")
                // Error on removing device. Show error dialog with an option to force remove.
                updateRemoveDeviceState(RemoveDeviceState.ForceRemove(deviceId))
                return@launch
            }
            // Remove device from the app's devices repository.
            devicesRepository.removeDevice(deviceId)
            // Notify UI so we navigate back to Home screen.
            updateRemoveDeviceState(RemoveDeviceState.Removed(deviceId))
        }
    }

    /**
     * Updates the remove device state in the UI state.
     */
    fun updateRemoveDeviceState(
        removeState: RemoveDeviceState
    ) {
        _deviceUiState.update {
            it.copy(removeDeviceState = removeState)
        }
    }


    // Removes the device from the app's devices repository, and does not unlink the fabric
    // from the device.
    // This function is called after removeDevice() has failed trying to unlink the device
    // and the user has confirmed that the device should still be removed from the app's device
    // repository.
    fun removeDeviceWithoutUnlink(deviceId: Long) {
        viewModelScope.launch {
            // Remove device from the app's devices repository.
            devicesRepository.removeDevice(deviceId)
            _deviceRemovalCompleted.value = true
            // Notify UI so we navigate back to Home screen.
            updateRemoveDeviceState(RemoveDeviceState.Removed(deviceId))
        }
    }


    // -----------------------------------------------------------------------------------------------
    // Device state (On/Off)

    fun updateDeviceStateOn(deviceUiModel: DeviceUiModel, isOn: Boolean) {
        viewModelScope.launch {
            try {
                clustersHelper.setOnOffDeviceStateOnOffCluster(
                    deviceUiModel.device.deviceId,
                    isOn,
                    0xd // TODO: use proper endpoint
                )
                // We observe state changes there, so we'll get these updates
                devicesStateRepository.updateDeviceState(deviceUiModel.device.deviceId, true, isOn)
            } catch (e: Throwable) {
                Log.e("UpdateDeviceState", "Failed setting on/off state on device: ${e.message}")
            }
        }
    }

    private fun stopDevicePeriodicPing() {
        devicePeriodicPingEnabled = false
    }

    fun showRemoveDeviceAlertDialog() {
        Log.d("AAA", "showRemoveDeviceAlertDialog")
        _showRemoveDeviceAlertDialog.value = true
    }

    fun dismissRemoveDeviceDialog() {
        Log.d("AAA", "dismissRemoveDeviceDialog")
        _showRemoveDeviceAlertDialog.value = false
    }

}