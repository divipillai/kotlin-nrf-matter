package no.nordicsemi.nrf.matter.ui.unsupported

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.cloudy
import no.nordicsemi.nrf.matter.commission.DecommissionDevice
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.DeviceUiModel
import no.nordicsemi.nrf.matter.theme.NordicTheme
import no.nordicsemi.nrf.matter.ui.BasicInformationBottomSheet
import no.nordicsemi.nrf.matter.ui.light.InfoItem
import kotlin.time.Clock

/*
 * Copyright (c) 2025, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
@Composable
internal fun UnsupportedItem(
    device: DeviceUiModel,
    title: String,
    subtitle: String,
    onDecommission: (DeviceId) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var showMatterDeviceInfo by rememberSaveable { mutableStateOf(false) }

    OutlinedCard(
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                isExpanded = !isExpanded
            }
            .then(if(showMatterDeviceInfo) Modifier.cloudy() else Modifier)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val boxColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
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
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
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
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { isExpanded = !isExpanded }
            )
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            Text(
                text = "The local application profile has not been implemented yet. Basic Device Information metadata is still available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }
        if (isExpanded) {
            AnimatedVisibility(isExpanded) {
                Column {
                    // Matter Device information section
                    HorizontalDivider()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
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
                                value = device.device.vendorName ?: "UNKNOWN",
                                modifier = Modifier.weight(1f)
                            )
                            InfoItem(
                                label = "Firmware",
                                value = device.device.softwareVersion ?: "UNKNOWN",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Decommission device
                    DecommissionDevice(device.device.deviceId, onDecommission)
                }

            }
        }

        // Basic Information Bottom Sheet Dialog
        if (showMatterDeviceInfo) {
            BasicInformationBottomSheet(device, onDismiss = { showMatterDeviceInfo = false })
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun UnsupportedItemPreview() {
    NordicTheme {
        UnsupportedItem(
            device = DeviceUiModel(
                device = Device(
                    deviceId = DeviceId("123456789"),
                    vendorName = "Nordic Semiconductor",
                    softwareVersion = "1.0.0",
                    dateCommissioned = Clock.System.now().toEpochMilliseconds(),
                    vendorId = "3456",
                    productId = "1234",
                    deviceType = DeviceType.UNSUPPORTED,
                    name = "Unsupported Device",
                    productName = "Matter Unsupported Device",
                    uniqueId = null,
                    specificationVersion = null,
                    serialNumer = null,
                    deviceMatterInfo = emptyList()
                ),
                isOnline = true,
                isOn = false
            ),
            title = "Unsupported Device",
            subtitle = "This device type is not supported yet.",
            onDecommission = {}
        )
    }
}