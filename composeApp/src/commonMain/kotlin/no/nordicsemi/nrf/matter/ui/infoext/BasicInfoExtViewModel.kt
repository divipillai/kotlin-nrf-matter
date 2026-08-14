package no.nordicsemi.nrf.matter.ui.infoext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.nrf.matter.cluster.BasicInfoExtCluster
import no.nordicsemi.nrf.matter.domain.UiState
import no.nordicsemi.nrf.matter.ui.device.ClusterViewModel

class BasicInfoExtViewModel(
    private val cluster: BasicInfoExtCluster,
    scope: CoroutineScope,
) : ClusterViewModel(scope) {

    private val _randomNumber = MutableStateFlow<UiState<Int>>(UiState.Idle())
    val randomNumber = _randomNumber.asStateFlow()

    fun generateRandomNumber() {
        _randomNumber.value = UiState.Loading()
        send(onFailure = { _randomNumber.value = UiState.Error("Could not generate a number.") }) {
            _randomNumber.value = UiState.Success(cluster.generateRandomNumber().toInt())
        }
    }
}
