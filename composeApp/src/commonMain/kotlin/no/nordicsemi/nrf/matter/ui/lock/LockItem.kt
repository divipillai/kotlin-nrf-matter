package no.nordicsemi.nrf.matter.ui.lock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.screens.DeviceItemContainer
import no.nordicsemi.nrf.matter.theme.NordicTheme
import no.nordicsemi.nrf.matter.ui.TestDeviceLockDoor
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.door_lock
import nrfmatterformobile.composeapp.generated.resources.door_lock_open_right
import org.jetbrains.compose.resources.painterResource

// Lock Item
@Composable
internal fun LockItem(
    device: DeviceUiModel,
    onLockUnlockDoor: (deviceId: DeviceId, value: Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val isLocked = device.isOn
    val icon = if (isLocked)
        painterResource(Res.drawable.door_lock)
    else painterResource(Res.drawable.door_lock_open_right)

    DeviceItemContainer(
        icon = icon,
        title = "Front Door",
        subtitle = "Smart Lock",
        onDeviceClick = onClick
    ) {
        Surface(
            color = Color.LightGray.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.clickable {
                onLockUnlockDoor(device.device.deviceId, !isLocked)
            }
        ) {
            Text(
                if (isLocked) "Locked" else "Unlocked",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE11D48)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LockItemPreview() {
    NordicTheme {
        LockItem(
            onLockUnlockDoor = { _, _ -> },
            device = TestDeviceLockDoor,
            onClick = {}
        )
    }
}
