package no.nordicsemi.nrf.matter.ui

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.nrf.matter.HomeViewModel
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.screens.DeviceItemContainer
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.light_bulb
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ManufacturerSpecItem(
    homeViewModel: HomeViewModel,
    device: DeviceUiModel,
    enabled: Boolean,
    updateDeviceState: (deviceId: DeviceId, Boolean) -> Unit,
    onClick: () -> Unit
) {
    val isButtonOn = homeViewModel.subscribeToButtonChanges(device.device.deviceId)
        .collectAsStateWithLifecycle(initialValue = false)
        .value
    val data = device.device.deviceMatterInfo.first().manufacturerSpecificData!! // Shouldn't be null for this device.
    DeviceItemContainer(
        icon = painterResource(Res.drawable.light_bulb),
        title = data.name,
        subtitle = "Turn light ON or OFF",
        isOnline = enabled,
        onDeviceClick = onClick
    ) {
        Switch(
            checked = enabled,
            onCheckedChange = {
                updateDeviceState(device.device.deviceId, it)
            }
        )

        Switch(
            checked = isButtonOn,
            onCheckedChange = {
                updateDeviceState(device.device.deviceId, it)
            },
            enabled = false,
        )
    }
}