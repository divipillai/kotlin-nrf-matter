package no.nordicsemi.nrf.matter.controller

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.DeviceBinding
import no.nordicsemi.nrf.matter.model.DeviceId

interface BindingController {

    val bindingLogs: Flow<String>

    suspend fun bind(
        sourceNodeId: DeviceId,
        sourceEndpoint: Int,
        targetNodeId: DeviceId,
        targetEndpoint: Int,
        clusterId: Long
    )
}