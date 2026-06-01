package no.nordicsemi.nrf.matter.ui.switch

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import no.nordicsemi.nrf.matter.device.BindingUiState
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceBinding
import no.nordicsemi.nrf.matter.model.DeviceController
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.repository.BindingRepository
import no.nordicsemi.nrf.matter.repository.DevicesStateRepository
import no.nordicsemi.nrf.matter.ui.CommandHandler

private const val ON_OFF_CLUSTER_ID: Long = 0x0006L

class SwitchCommandHandler(
    private val devicesStateRepository: DevicesStateRepository,
    private val deviceController: DeviceController,
    private val bindingRepository: BindingRepository,
) : CommandHandler {

    fun handleOutlet(
        device: Device,
        isOn: Boolean
    ) = withUiState {
        val endpoint = resolveEndpoint(device, clusterId = ON_OFF_CLUSTER_ID)

        try {
            devicesStateRepository.updateDeviceState(
                deviceId = device.deviceId,
                isOnline = true,
                isOn = isOn
            )

            deviceController.handleOutlet(
                deviceId = device.deviceId,
                isSwitchOn = isOn,
                endpoint = endpoint
            )

            isOn
        } catch (e: Exception) {

            devicesStateRepository.updateDeviceState(
                deviceId = device.deviceId,
                isOnline = false,
                isOn = !isOn
            )

            !isOn
        }
    }

    fun bind(
        switchNodeId: DeviceId,
        lightNodeId: DeviceId,
    ): Flow<BindingUiState> = flow {
        emit(UiState.Loading())

        try {
            deviceController.bind(
                sourceNodeId = switchNodeId,
                sourceEndpoint = 1, // TODO: Add a function call that looks the endpoint of the switch where binding is configured.
                targetNodeId = lightNodeId,
                targetEndpoint = 1, // TODO: Add a function call that looks the endpoint of the light where cluster id is configured.
                clusterId = ON_OFF_CLUSTER_ID, // TODO: Change it to provide the cluster id based on the type of binding.
            )

            val bindingDevice = DeviceBinding(
                id = "${switchNodeId}_${lightNodeId}_${ON_OFF_CLUSTER_ID}",
                sourceNodeId = switchNodeId,
                targetNodeId = lightNodeId,
                sourceEndpoint = 1,
                targetEndpoint = 1,
                clusterId = ON_OFF_CLUSTER_ID
            )

            bindingRepository.save(bindingDevice)

            emit(UiState.Success(bindingDevice))

        } catch (e: Exception) {
            Napier.e(e) { "Binding failed: ${e.message}" }
            emit(UiState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)
}
