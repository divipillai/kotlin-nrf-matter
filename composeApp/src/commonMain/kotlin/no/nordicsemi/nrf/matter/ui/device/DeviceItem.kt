package no.nordicsemi.nrf.matter.ui.device

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
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.cloudy
import no.nordicsemi.nrf.matter.cluster.BasicInfoExtCluster
import no.nordicsemi.nrf.matter.cluster.Cluster
import no.nordicsemi.nrf.matter.cluster.DoorLockCluster
import no.nordicsemi.nrf.matter.cluster.LevelControlCluster
import no.nordicsemi.nrf.matter.cluster.ManufacturerSpecCluster
import no.nordicsemi.nrf.matter.cluster.OnOffCluster
import no.nordicsemi.nrf.matter.commission.DecommissionDevice
import no.nordicsemi.nrf.matter.composeapp.generated.resources.Res
import no.nordicsemi.nrf.matter.composeapp.generated.resources.door_lock
import no.nordicsemi.nrf.matter.composeapp.generated.resources.door_lock_open_right
import no.nordicsemi.nrf.matter.domain.UiState
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.model.LockDeviceState
import no.nordicsemi.nrf.matter.theme.NordicSun
import no.nordicsemi.nrf.matter.ui.BasicInformationBottomSheet
import no.nordicsemi.nrf.matter.ui.light.InfoItem
import org.jetbrains.compose.resources.painterResource

// Lock Item
@Composable
internal fun DeviceItem(
    device: DeviceUiModel,
    lockState: UiState<LockDeviceState>,
    onLockUnlockDoor: (deviceId: DeviceId, value: Boolean) -> Unit,
    onDecommission: (DeviceId) -> Unit,
) {
    DeviceItemContainer(
        deviceUiModel = device,
        title = "Front Door",
        subtitle = "Smart Lock",
        lockState = lockState,
        onLockUnlockDoor = onLockUnlockDoor,
        onDecommission = onDecommission
    )

}

@Composable
fun DeviceItemContainer(
    deviceUiModel: DeviceUiModel,
    title: String,
    subtitle: String,
    lockState: UiState<LockDeviceState>,
    clusters: List<Cluster>,
    onLockUnlockDoor: (deviceId: DeviceId, value: Boolean) -> Unit,
    onDecommission: (DeviceId) -> Unit,
) {
    var isLocked by remember { mutableStateOf(false) }

    LaunchedEffect(lockState) {
        (lockState as? UiState.Success)?.let {
            isLocked = it.data == LockDeviceState.LOCKED
        }
    }

    val icon = if (isLocked)
        painterResource(Res.drawable.door_lock)
    else painterResource(Res.drawable.door_lock_open_right)

    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var showMatterDeviceInfo by rememberSaveable { mutableStateOf(false) }

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = if (isLocked) BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(0.3f)
        ) else CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                isExpanded = !isExpanded
            }
            .then(if (showMatterDeviceInfo) Modifier.cloudy() else Modifier)
    ) {

        DeviceHeader(
            isOn = isLocked,
            icon = icon,
            title = title,
            subtitle = subtitle,
            bindingCapable = false
        ) {
            when (deviceUiModel.device.deviceType) {

                DeviceType.LIGHT_ON_OFF,
                DeviceType.DIMMABLE_LIGHT -> OnOffActionItem(

                )
                DeviceType.DOOR_LOCK -> LockActionItem(

                )
                DeviceType.LIGHT_SWITCH,
                DeviceType.OUTLET,
                DeviceType.UNSUPPORTED,
                DeviceType.COLOR_TEMPERATURE_LIGHT,
                DeviceType.EXTENDED_COLOR_LIGHT,
                DeviceType.MANUFACTURER_SPECIFIC_DEVICE -> TODO()
            }
        }
        if (isExpanded) {
            AnimatedVisibility(isExpanded) {
                Column {
                    // Matter Device information section
                    HorizontalDivider()

                    clusters.forEach {
                        when (it) {
                            is BasicInfoExtCluster -> BasicInfoExtControlItem()
                            is LevelControlCluster -> LevelControlItem()
                            is ManufacturerSpecCluster -> ManufacturerSpecControlItem()
                            is OnOffCluster,
                            is DoorLockCluster -> { }
                        }
                    }

                    SharedSection(deviceUiModel, showMatterDeviceInfo) { showMatterDeviceInfo = it }

                    // Decommission device
                    DecommissionDevice(deviceUiModel.device.deviceId, onDecommission)
                }

            }
        }

        // Basic Information Bottom Sheet Dialog
        if (showMatterDeviceInfo) {
            BasicInformationBottomSheet(deviceUiModel, onDismiss = { showMatterDeviceInfo = false })
        }

    }
}

@Composable
private fun SharedSection(
    deviceUiModel: DeviceUiModel,
    showMatterDeviceInfo: Boolean,
    onShowMatterDeviceInfoChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onShowMatterDeviceInfoChange(true)
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
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (showMatterDeviceInfo) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = "Info",
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoItem(
                label = "Vendor",
                value = deviceUiModel.device.vendorName ?: "UNKNOWN",
                modifier = Modifier.weight(1f)
            )
            InfoItem(
                label = "Firmware",
                value = deviceUiModel.device.softwareVersion ?: "UNKNOWN",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DeviceHeader(
    isOn: Boolean,
    icon: Painter,
    title: String,
    subtitle: String,
    bindingCapable: Boolean,
    mainAction: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val boxColor = if (isOn)
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
                tint = if (isOn)
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
            if (bindingCapable) {
                Text(
                    text = "Binding capability",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f)
                )
            }
        }

        mainAction()
    }
}
