package no.nordicsemi.nrf.matter.ui.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.screens.DeviceItemContainer
import no.nordicsemi.nrf.matter.theme.NordicSun
import no.nordicsemi.nrf.matter.theme.NordicTheme
import no.nordicsemi.nrf.matter.ui.TestDeviceLockDoor
import no.nordicsemi.nrf.matter.ui.light.BasicInformationBottomSheet
import no.nordicsemi.nrf.matter.ui.light.BrightnessControlCard
import no.nordicsemi.nrf.matter.ui.light.DecommissionDevice
import no.nordicsemi.nrf.matter.ui.light.InfoItem
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
                text = if (isLocked) "Locked" else "Unlocked",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
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

@Composable
fun LockControlItem(
    icon: Painter,
    title: String,
    subtitle: String,
    isOnline: Boolean = true,
    onDeviceClick: () -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    var showMatterDeviceInfo by rememberSaveable { mutableStateOf(false) }

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = if (isOnline) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(0.3f)
        ) else CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onDeviceClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val boxColor = if (isOnline)
                NordicSun
            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        boxColor,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isOnline)
                        MaterialTheme.colorScheme.primary else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f)
                )

            }

            Surface(
                color = Color.LightGray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable {
                    // todo: lock/unlock device
                }
            ) {
                Text(
                    text = if (isOnline) "Locked" else "Unlocked",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE11D48)
                )
            }
        }
        if (isExpanded) {
            AnimatedVisibility(isExpanded) {
                Column {
                    // Matter Device information section
                    HorizontalDivider()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp).clickable {
                                showMatterDeviceInfo = true
                            },
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,

                                )
                            Text(
                                text = "Matter Device information",
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoItem(
                                label = "Vendor",
                                value = "Nordic Semi",
                                modifier = Modifier.weight(1f)
                            )
                            InfoItem(
                                label = "Firmware",
                                value = "v1.2.4-stable",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Decommission device
                    DecommissionDevice()
                }

            }
        }

        // Basic Information Bottom Sheet Dialog
        if (showMatterDeviceInfo) {
            BasicInformationBottomSheet(onDismiss = { showMatterDeviceInfo = false })
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun LockControlItemPreview() {
    LockControlItem(
        icon = painterResource(Res.drawable.door_lock),
        title = "Front Door",
        subtitle = "Smart Lock",
        onDeviceClick = {},
    )
}


