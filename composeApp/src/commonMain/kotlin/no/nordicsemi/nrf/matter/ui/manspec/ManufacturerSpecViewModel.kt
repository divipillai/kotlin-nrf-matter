package no.nordicsemi.nrf.matter.ui.manspec

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.nrf.matter.cluster.ManufacturerSpecCluster
import no.nordicsemi.nrf.matter.domain.UiState
import no.nordicsemi.nrf.matter.ui.device.ClusterViewModel

data class ManufacturerSpecState(
    val isLedOn: UiState<Boolean> = UiState.Idle(),
    val isButtonPressed: UiState<Boolean> = UiState.Idle(),
)

class ManufacturerSpecViewModel(
    private val cluster: ManufacturerSpecCluster,
    scope: CoroutineScope,
) : ClusterViewModel(scope) {

    private val _state = MutableStateFlow(ManufacturerSpecState())
    val state = _state.asStateFlow()

    init {
        observe({ cluster.observeLed() }) { isOn ->
            _state.update { it.copy(isLedOn = UiState.Success(isOn)) }
        }
        observe({ cluster.observeButton() }) { isPressed ->
            _state.update { it.copy(isButtonPressed = UiState.Success(isPressed)) }
        }
    }

    fun setLed(isOn: Boolean) {
        _state.update { it.copy(isLedOn = UiState.Loading()) }
        send(onFailure = { _state.update { it.copy(isLedOn = UiState.Error("Could not set the LED.")) } }) {
            cluster.setLed(isOn)
            _state.update { it.copy(isLedOn = UiState.Success(isOn)) }
        }
    }
}
