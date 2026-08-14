package no.nordicsemi.nrf.matter.ui.device

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.nrf.matter.cluster.LevelControlCluster

data class LevelControlState(
    val brightness: Float = 0f,
    val isEnabled: Boolean = true,
)

class LevelControlViewModel(
    private val cluster: LevelControlCluster,
    scope: CoroutineScope,
) : ClusterViewModel(scope) {

    private val _state = MutableStateFlow(LevelControlState())
    val state = _state.asStateFlow()

    init {
        observe({ cluster.observeLevel() }) { level ->
            _state.update { it.copy(brightness = level.toBrightness()) }
        }
    }

    /** Moves the slider without touching the device; the value is sent by [commitBrightness]. */
    fun setBrightness(brightness: Float) {
        _state.update { it.copy(brightness = brightness) }
    }

    /** Sends the brightness the user has selected. */
    fun commitBrightness() {
        val brightness = _state.value.brightness
        _state.update { it.copy(isEnabled = false) }
        send(onFailure = { _state.update { it.copy(isEnabled = true) } }) {
            cluster.setLevel(brightness.toLevel())
            _state.update { it.copy(isEnabled = true) }
        }
    }
}
