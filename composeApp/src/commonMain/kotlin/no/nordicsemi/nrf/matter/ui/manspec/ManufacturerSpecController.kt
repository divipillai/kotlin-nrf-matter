package no.nordicsemi.nrf.matter.ui.manspec

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.ui.MatterController

class ManufacturerSpecController(
    private val device: DeviceUiModel,
    private val commandHandler: ManufacturerSpecCommandHandler,
    private val scope: CoroutineScope,
) : MatterController {

    private val ledState = MutableStateFlow<UiState<Boolean>>(UiState.Success(device.isOn))
    private val randomNumber = MutableStateFlow<UiState<Int>>(UiState.Idle())
    private val buttonState = commandHandler.subscribeToButtonChanges(device.device.deviceId)
        .stateIn(scope, SharingStarted.Eagerly, UiState.Idle())

    private fun setLed(value: Boolean) {
        commandHandler.handleLed(device.device, value)
            .onEach { ledState.value = it }
            .launchIn(scope)
    }

    private fun generateRandomNumber() {
        commandHandler.generateRandomNumber(device.device.deviceId)
            .onEach { randomNumber.value = it }
            .launchIn(scope)
    }

    @Composable
    override fun Item(onDeviceClick: (DeviceId) -> Unit) {
        return ManufacturerSpecItem(
            device,
            manufacturerSpecificData = device.device.deviceMatterInfo.firstNotNullOf { it.manufacturerSpecificData },
            isLedOn = ledState.collectAsStateWithLifecycle(initialValue = UiState.Idle()).value,
            isButtonOn = buttonState.collectAsStateWithLifecycle(initialValue = UiState.Idle()).value,
            randomNumber = randomNumber.collectAsStateWithLifecycle(initialValue = UiState.Idle()).value,
            setLed = ::setLed,
            generateRandomNumber = ::generateRandomNumber,
            onClick = { onDeviceClick(device.device.deviceId) },
        )
    }
}
