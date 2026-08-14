package no.nordicsemi.nrf.matter.ui.device

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable

@Composable
fun OnOffActionItem(
    isOn: Boolean,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = isOn,
        onCheckedChange = onCheckedChange,
        enabled = isEnabled
    )
}
