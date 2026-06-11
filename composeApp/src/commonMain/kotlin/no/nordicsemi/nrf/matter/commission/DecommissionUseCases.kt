package no.nordicsemi.nrf.matter.commission

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.model.DeviceId
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
class DecommissionUseCases(
    private val deviceController: DeviceController,
    private val devicesStateRepository: DevicesStateRepository,
    private val devicesRepository: DevicesRepository,
) {

    fun decommissionDevice(deviceId: DeviceId): Flow<DecommissionState> = flow {
        emit(DecommissionState.InProgress)
        try {
            deviceController.unlinkDevice(deviceId)
            NordicLogger.info("Device $deviceId decommissioned successfully.")
            emit(DecommissionState.Success(deviceId))

            // Remove the device from the local repository after successful unlinking.
            NordicLogger.info("Removing device $deviceId from local repository.")
            devicesStateRepository.removeDevice(deviceId)
            devicesRepository.removeDevice(deviceId)
        } catch (e: Exception) {
            NordicLogger.error("Decommissioning failed: ${e.message}", e)
            emit(DecommissionState.Error(deviceId, e.message))
        }
    }.flowOn(Dispatchers.IO)

    fun forceRemoveDevice(deviceId: DeviceId): Flow<DecommissionState> = flow {
        emit(DecommissionState.InProgress)
        try {
            // Force remove the device from the local repository without unlinking
            NordicLogger.info("Force removing device $deviceId without unlinking.")
            devicesStateRepository.removeDevice(deviceId)
            devicesRepository.removeDevice(deviceId)

            NordicLogger.info("Device $deviceId removed successfully from local repository.")
            emit(DecommissionState.Success(deviceId))
        } catch (e: Exception) {
            NordicLogger.error("Force removal failed: ${e.message}", e)
            emit(DecommissionState.Error(deviceId, e.message))
        }
    }.flowOn(Dispatchers.IO)

}