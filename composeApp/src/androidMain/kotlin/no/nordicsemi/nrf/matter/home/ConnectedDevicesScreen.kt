package no.nordicsemi.nrf.matter.home

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nordicsemi.nrf.matter.R
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceState
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.ui.DeviceItemContainer
import no.nordicsemi.nrf.matter.ui.SectionTitle
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

val MatterGreen = Color(0xFF22C55E)

// Specific Device: Dimmable Light
@Preview(showBackground = true)
@Composable
fun DimmableLightItem() {
    DeviceItemContainer(
        icon = painterResource(R.drawable.light_bulb_smart_light),
        title = "Living Room Lamp",
        subtitle = "Dimmable Light",
        onDeviceClick = {
            /* TODO: Add onClick handler */
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("50%", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = 0.5f,
                modifier = Modifier
                    .width(64.dp)
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color(0xFFF59E0B),
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }
}

// Specific Device: Smart Switch
@Composable
fun SwitchDeviceItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onOnOffClick: (deviceId: Long, value: Boolean) -> Unit,
    onDeviceClick: () -> Unit
) {
    DeviceItemContainer(
        icon = painterResource(R.drawable.light_fixture),// TODO: Change it to the Power icon
        title = title,
        subtitle = subtitle,
        isOnline = checked,
        onDeviceClick = { onDeviceClick() }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = {
                onOnOffClick(1L, it)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchDeviceItemPreview() {
    SwitchDeviceItem(
        title = "Living Room Lamp",
        subtitle = "Smart Switch",
        checked = true,
        onOnOffClick = { _, _ -> },
        onDeviceClick = {},
    )
}

@Preview(showBackground = true)
@Composable
fun HeaderSection() {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Pic Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    "Welcome Home",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    "My Devices",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            // Matter Badge
            Surface(
                color = MatterGreen.copy(alpha = 0.1f),
                shape = CircleShape,
                border = BorderStroke(1.dp, MatterGreen.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MatterGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "MATTER",
                        color = MatterGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterChipsRow() {
    val filters = listOf("All", "Living Room", "Kitchen", "Bedroom")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filters) { filter ->
            val isSelected = filter == "All"
            Surface(
                shape = CircleShape,
                border = if (isSelected) null else BorderStroke(
                    1.dp,
                    Color.LightGray.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else Color.Unspecified,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
internal fun DeviceList(
    devicesList: List<DeviceUiModel>,
    onDeviceClick: (DeviceUiModel) -> Unit,
    onOnOffClick: (deviceId: Long, value: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // --- Section: Lights ---
        this.items(devicesList, key = { device -> device.device.deviceId }) { device ->
            SectionTitle("Lights")
            SwitchDeviceItem(
                title = device.device.name ?: "Living Room Lamp",
                subtitle = "Smart Light",
                checked = device.isOn,
                onOnOffClick = { deviceId, value -> onOnOffClick(deviceId, value) },
            ) { onDeviceClick(device) }

        }
        /*
                item { SectionHeader("Lights") }
                item { DimmableLightItem() }
                item {
                    SwitchDeviceItem(
                        title = "Hallway Light",
                        subtitle = "Smart Light",
                        initialState = false
                    )
                }

                        // --- Section: Power & Energy ---
                        item { SectionHeader("Power & Energy") }
                        item {
                            SwitchDeviceItem(
                                title = "Coffee Maker",
                                subtitle = "Smart Plug",
                                initialState = true
                            )
                        }

                        // --- Section: Climate & Security ---
                        item { SectionHeader("Climate & Security") }
                        item { ThermostatItem() }
                        item { LockItem() }

                        // --- Empty State / Add Suggestion ---
                        item { AddDeviceSuggestion() }
                  */
    }
}

// Thermostat Item
@Preview(showBackground = true)
@Composable
fun ThermostatItem() {
    DeviceItemContainer(
        icon = painterResource(R.drawable.temperature),
        title = "Downstairs AC",
        subtitle = "Target: 70°F", onDeviceClick = {}
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "72°",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Cooling", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

// Lock Item
@Preview(showBackground = true)
@Composable
fun LockItem() {
    DeviceItemContainer(
        icon = painterResource(R.drawable.light_bulb_smart_light),// TODO: Change it to the door lock icon.
        title = "Front Door",
        subtitle = "Smart Lock",
        onDeviceClick = {}
    ) {
        Surface(
            color = Color.LightGray.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "LOCKED",
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
fun AddDeviceSuggestion() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            // Note: For a true dashed border in Compose, you'd use a custom DrawModifier
            .border(2.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.AddCircle,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(32.dp)
            )
            Text(
                "Discover new devices",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoDevicesScreen(
    onAddDeviceClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyStateIllustration()

        Spacer(modifier = Modifier.height(32.dp))

        // Text Content
        Text(
            text = "Let's get connected",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "It looks like you haven't added any Matter accessories yet. Connect your first device to start controlling your home intelligently.",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSystemInDarkTheme()) Color(0xFF9DABB9) else Color(0xFF637588),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action Button
        Button(
            onClick = { onAddDeviceClick() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .widthIn(max = 320.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add New Device", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { /* TODO */ }) {
            Text(
                "What is Matter?",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStateIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        // Background Glow/Pulse
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )

        Surface(
            modifier = Modifier.size(200.dp),
            shape = RoundedCornerShape(20.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.no_matter_devices),
                contentDescription = null,
                modifier = Modifier.padding(40.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeTopAppBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "My Devices",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.Menu, contentDescription = "Menu",
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}

// -----------------------------------------------------------------------------------------------
// Constant objects used in Compose Preview

// DeviceState -- Online and On
private val DeviceState_OnlineOn =
    DeviceState(
        dateCaptured = Clock.System.now(),
        deviceId = 1L,
        on = true,
        online = true

    )

// DeviceState -- Offline
private val DeviceState_Offline =
    DeviceState(
        dateCaptured = Clock.System.now(),
        deviceId = 1L,
        on = true,
        online = false

    )

private val DeviceTest =
    Device(
        dateCommissioned = 123456789L,
        vendorId = "1234",
        productId = "5678",
        deviceType = DeviceType.LIGHT_ON_OFF,
        deviceId = 1L,
        name = "Living Room Light",
        productName = "My Light",
        vendorName = "MyVendor"

    )
