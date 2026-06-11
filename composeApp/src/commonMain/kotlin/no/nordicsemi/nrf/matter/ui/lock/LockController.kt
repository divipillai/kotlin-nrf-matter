package no.nordicsemi.nrf.matter.ui.lock

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.MatterController

class LockController(
    private val device: DeviceUiModel,
    private val commandHandler: LockCommandHandler,
    private val scope: CoroutineScope,
) : MatterController {

    val lockState = MutableStateFlow<UiState<Boolean>>(UiState.Idle())

    fun setLock(device: Device, isOn: Boolean) {
        commandHandler.handleLock(device, isOn)
            .onEach { lockState.value = it }
            .launchIn(scope)
    }

    @Composable
    override fun Item(onDecommission: (DeviceId) -> Unit) {
        LockItem(
            device = device,
            onLockUnlockDoor = { deviceId, state ->
                setLock(device.device, state)
            },
            onDecommission = onDecommission,
        )
    }
}
