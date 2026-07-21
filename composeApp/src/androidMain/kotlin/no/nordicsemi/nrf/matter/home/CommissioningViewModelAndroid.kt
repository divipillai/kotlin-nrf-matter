package no.nordicsemi.nrf.matter.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chip.devicecontroller.ChipDeviceControllerException
import com.google.android.gms.home.matter.commissioning.CommissioningResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.chip.ClustersHelper
import no.nordicsemi.nrf.matter.chip.MatterBasicInfoProvider
import no.nordicsemi.nrf.matter.commission.CommissioningException
import no.nordicsemi.nrf.matter.commission.Stage
import no.nordicsemi.nrf.matter.commission.toCommissioningException
import no.nordicsemi.nrf.matter.domain.OperationResult
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.toDeviceId
import no.nordicsemi.nrf.matter.repository.DevicesRepository
import kotlin.time.Clock

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

class CommissioningViewModelAndroid(
    private val basicInfoProvider: MatterBasicInfoProvider,
    private val clustersHelper: ClustersHelper,
    private val devicesRepository: DevicesRepository,
) : ViewModel() {

    val nextNodeId = MutableStateFlow<DeviceId?>(null)
    val deviceEvent = Channel<OperationResult<Device>>()

    init {
        viewModelScope.launch {
            nextNodeId.value = devicesRepository.incrementAndReturnLastDeviceId()
        }
    }

    fun gpsCommissioningDeviceSucceeded(gpsCommissioningResult: CommissioningResult) {
        viewModelScope.launch {
            val deviceId = gpsCommissioningResult.token?.toDeviceId() ?: run {
                deviceEvent.send(OperationResult.Error(Exception("Token is missing.")))
                return@launch
            }
            try {
                val basicInfo = catchAndThrow(Stage.READ_BASIC_INFORMATION) {
                    basicInfoProvider.fetchBasicInfo(deviceId)
                }

                val deviceMatterInfoList = catchAndThrow(Stage.READ_DESCRIPTOR_CLUSTER) {
                    clustersHelper.fetchDeviceMatterInfo(deviceId)
                }

                val deviceType = mutableStateListOf<DeviceType>()
                deviceMatterInfoList.forEach {
                    // Ignore the first endpoint because this is the root node.
                    if (it.endpoint != 0) {
                        // Get the device type from the rest of the endpoint.
                        it.types.forEach { type ->
                            val type = convertToAppDeviceType(type)
                            deviceType.add(type)
                        }
                    }
                }
                val device = Device(
                    vendorName = basicInfo.vendorName,
                    productName = basicInfo.productName,
                    dateCommissioned = Clock.System.now()
                        .toEpochMilliseconds(), // Date when the device was commissioned.
                    vendorId = basicInfo.vendorId.toString(),
                    productId = basicInfo.productId.toString(),
                    deviceType = deviceType.firstOrNull() ?: DeviceType.UNSUPPORTED,
                    deviceId = deviceId,
                    name = gpsCommissioningResult.deviceName,
                    uniqueId = basicInfo.uniqueId.toString(),
                    softwareVersion = basicInfo.softwareVersion,
                    serialNumer = basicInfo.serialNumber,
                    specificationVersion = basicInfo.specificationVersion,
                    deviceMatterInfo = deviceMatterInfoList,
                )

                deviceEvent.send(OperationResult.Success(device))
            } catch (t: Throwable) {
                NordicLogger.error("Commissioning failed", t)
                deviceEvent.send(OperationResult.Error(t.toCommissioningException(deviceId)))
            }
        }
    }

    private suspend fun <T> catchAndThrow(stage: Stage, block: suspend () -> T): T {
        try {
            return block()
        } catch (t: ChipDeviceControllerException) {
            throw CommissioningException(
                nextNodeId.value,
                stage,
                t.errorCode.toInt(),
                t.message ?: ""
            )
        } catch (t: Throwable) {
            throw CommissioningException(
                nextNodeId.value,
                stage,
                null,
                t.message ?: ""
            )
        }
    }

    private fun convertToAppDeviceType(matterDeviceType: Long): DeviceType {
        return when (matterDeviceType) {
            256L -> DeviceType.LIGHT_ON_OFF // 0x0100 On/Off Light
            257L -> DeviceType.DIMMABLE_LIGHT // 0x0101 Dimmable Light
            259L -> DeviceType.LIGHT_SWITCH// 0x0103 On/Off Light Switch
            260L -> DeviceType.LIGHT_SWITCH // 0x0104 On/Off Outlet

            266L -> DeviceType.OUTLET // 0x010A (On/Off Plug-in Unit)
            268L -> DeviceType.COLOR_TEMPERATURE_LIGHT // 0x010C Color Temperature Light
            269L -> DeviceType.EXTENDED_COLOR_LIGHT // 0x010D Extended Color Light
            10L -> DeviceType.DOOR_LOCK // 0x000A door lock // todo need to review the hex value
//            11L ->   Door Lock Controller // (0x000B)
            0xFFF10001 -> DeviceType.MANUFACTURER_SPECIFIC_DEVICE
            else -> DeviceType.UNSUPPORTED
        }
    }
}
