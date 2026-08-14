package no.nordicsemi.nrf.matter.ui.device

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.nrf.matter.cluster.OnOffCluster

data class OnOffState(
    val isOn: Boolean = false,
    val isEnabled: Boolean = true,
)

class OnOffViewModel(
    private val cluster: OnOffCluster,
    scope: CoroutineScope,
) : ClusterViewModel(scope) {

    private val _state = MutableStateFlow(OnOffState())
    val state = _state.asStateFlow()

    init {
        observe({ cluster.observeOnOff() }) { isOn ->
            _state.update { it.copy(isOn = isOn, isEnabled = true) }
        }
    }

    fun setOn(isOn: Boolean) {
        // The switch follows the request immediately and is restored if the device rejects it.
        _state.update { it.copy(isOn = isOn, isEnabled = false) }
        send(onFailure = { _state.update { it.copy(isOn = !isOn, isEnabled = true) } }) {
            cluster.setOn(isOn)
            _state.update { it.copy(isEnabled = true) }
        }
    }
}
