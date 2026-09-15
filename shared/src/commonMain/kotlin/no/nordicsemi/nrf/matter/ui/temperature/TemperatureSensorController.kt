package no.nordicsemi.nrf.matter.ui.temperature

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import no.nordicsemi.nrf.matter.cluster.TemperatureMeasurementCluster
import no.nordicsemi.nrf.matter.ui.device.ClusterController

data class TemperatureSensorState(
    val temperatureCelsius: Float = 0f,
)

class TemperatureSensorController(
    cluster: TemperatureMeasurementCluster,
    scope: CoroutineScope,
) : ClusterController(scope) {

    private val _state = MutableStateFlow(TemperatureSensorState())
    val state = _state.asStateFlow()

    init {
        cluster.observeMeasuredValue()
            .onEach { value -> _state.update { it.copy(temperatureCelsius = value.toFloat() / 100f) } }
            .launchIn(scope)
    }
}
