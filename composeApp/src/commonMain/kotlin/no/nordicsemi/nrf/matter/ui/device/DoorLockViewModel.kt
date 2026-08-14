package no.nordicsemi.nrf.matter.ui.device

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.nrf.matter.cluster.DoorLockCluster
import no.nordicsemi.nrf.matter.domain.UiState
import no.nordicsemi.nrf.matter.model.LockDeviceState

class DoorLockViewModel(
    private val cluster: DoorLockCluster,
    scope: CoroutineScope,
) : ClusterViewModel(scope) {

    private val _state = MutableStateFlow<UiState<LockDeviceState>>(UiState.Loading())
    val state = _state.asStateFlow()

    init {
        observe({ cluster.observeLockState() }) { lockState ->
            lockState.toLockDeviceState()?.let { _state.value = it.toUiState() }
        }
    }

    fun setLocked(isLocked: Boolean) {
        // The device reports the new state once the bolt has moved, until then the lock is busy.
        _state.value = UiState.Loading()
        send(onFailure = { _state.value = UiState.Error("Could not change the lock state.") }) {
            cluster.setLocked(isLocked)
        }
    }
}
