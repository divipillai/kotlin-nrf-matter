package no.nordicsemi.nrf.matter.ui.light

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.cloudy
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.theme.NordicSun
import no.nordicsemi.nrf.matter.ui.DeviceControlItem
import no.nordicsemi.nrf.matter.ui.TestDeviceLight
import no.nordicsemi.nrf.matter.ui.manspec.ControlCardContainer
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.light_bulb
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun LightItem(
    device: DeviceUiModel,
    updateDeviceState: (deviceId: DeviceId, Boolean) -> Unit,
    onClick: () -> Unit
) {
    DeviceControlItem(
        deviceId = device.device.deviceId,
        title = "Light",
        subtitle = "Turn light ON or OFF",
        icon = painterResource(Res.drawable.light_bulb),
        enabled = device.isOn,
        updateDeviceState = updateDeviceState,
        onClick = onClick
    )
}

@Composable
internal fun LightControlItem(
    deviceId: DeviceId,
    title: String,
    subtitle: String,
    icon: Painter,
    enabled: Boolean,
    updateDeviceState: (deviceId: DeviceId, Boolean) -> Unit,
    onClick: () -> Unit
) {
    var showMatterDeviceInfo by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = if (enabled) BorderStroke(
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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val boxColor = if (enabled)
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
                    tint = if (enabled)
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
                Text(
                    text = "Binding capability",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f)
                )

            }
            Switch(
                checked = enabled,
                onCheckedChange = {
                    updateDeviceState(deviceId, it)
                }
            )
        }
        AnimatedVisibility(isExpanded) {
            Column {
                // TODO: Add if statement to show brightness only if the device supports it.
                // Brightness control section
                HorizontalDivider()
                BrightnessControlCard()

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

@Composable
internal fun DecommissionDevice() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedCard(
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder(enabled = false),
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        // todo: remove device
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    "Remove/Decommission Device", fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ControlCardContainer(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrightnessControlCard(modifier: Modifier = Modifier) {
    var brightness by remember { mutableFloatStateOf(0.85f) } // TODO: it has to be read from the device.

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Brightness Control",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(brightness * 100).roundToInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Slider(
            value = brightness,
            onValueChange = { brightness = it },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                thumbColor = MaterialTheme.colorScheme.primary
            ),
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { MutableInteractionSource() },
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LightMode,
                contentDescription = "Low Brightness",
                tint = MaterialTheme.colorScheme.outline,
            )
            Icon(
                imageVector = Icons.Filled.LightMode,
                contentDescription = "High Brightness",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LightControlItemPreview() {
    LightControlItem(
        deviceId = DeviceId.Zero,
        title = "Light",
        subtitle = "Turn light ON or OFF",
        icon = painterResource(Res.drawable.light_bulb),
        enabled = true,
        updateDeviceState = { _, _ -> },
        {}
    )
}

@Preview(showBackground = true)
@Composable
fun BasicDeviceInformation() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Device Details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Matter Device Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Matter Cluster Index 0x0028 Reader",

                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = "These properties are fetched directly during CASE establishment from local cluster declarations:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TODO: Change it to show the items only if they are not null.
            // TODO: replace with the real device info.
            InfoRow(
                label = "Product Name",
                value = TestDeviceLight.device.productName.toString(),
                attrId = "0x0003"
            )
            InfoRow(
                label = "Vendor ID",
                value = "0x" + TestDeviceLight.device.vendorId.toString()
                    .uppercase(),
                attrId = "0x0002"
            )
            InfoRow(
                label = "Product ID",
                value = "0x" + TestDeviceLight.device.productId.toString()
                    .uppercase(),
                attrId = "0x0004"
            )
            InfoRow(
                label = "Vendor Name",
                value = TestDeviceLight.device.vendorName.toString(),
                attrId = "0x0001"
            )
            InfoRow(
                label = "Software Version",
                value = TestDeviceLight.device.softwareVersion
                    ?: "1.0.0",
                attrId = "0x0009"
            )
            InfoRow(
                label = "Serial Number",
                value = TestDeviceLight.device.serialNumer
                    ?: "123456789",
                attrId = "0x000F"
            )
            InfoRow(
                label = "Unique ID",
                value = TestDeviceLight.device.uniqueId
                    ?: "123456789",
                attrId = "0x0012"
            )
            InfoRow(
                label = "Specification Version",
                value = "${TestDeviceLight.device.specificationVersion}",
                attrId = "0x0013"
            )
        }
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Placeholder for others")
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicInformationBottomSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        BasicDeviceInformation()
    }

}

@Composable
fun InfoRow(label: String, value: String, attrId: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = attrId,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .padding(4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InfoRowPreview() {
    InfoRow(label = "Label", value = "Value", attrId = "AttrId")
}