package no.nordicsemi.nrf.matter.adapters

import no.nordicsemi.nrf.matter.controller.BindingController
import no.nordicsemi.nrf.matter.model.DeviceId

class BindingControllerImpl : BindingController {
    override suspend fun bind(
        sourceNodeId: DeviceId,
        sourceEndpoint: Int,
        targetNodeId: DeviceId,
        targetEndpoint: Int,
        clusterId: Long
    ) {
        TODO("Not yet implemented")
    }
}