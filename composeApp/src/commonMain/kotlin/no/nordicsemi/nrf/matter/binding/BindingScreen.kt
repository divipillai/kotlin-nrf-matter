package no.nordicsemi.nrf.matter.binding

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.SwapCalls
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.cloudy.cloudy
import multiplatform.network.cmptoast.ToastDuration
import multiplatform.network.cmptoast.ToastGravity
import multiplatform.network.cmptoast.showToast
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.model.DeviceBinding
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.toDeviceId
import no.nordicsemi.nrf.matter.theme.NordicSun
import no.nordicsemi.nrf.matter.theme.NordicTheme
import no.nordicsemi.nrf.matter.ui.AlertDialogView
import no.nordicsemi.nrf.matter.ui.DeviceTest_LIGHT
import org.koin.compose.viewmodel.koinViewModel

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
internal fun BindingsScreen(
) {
    val bindingViewModel: BindingViewModel = koinViewModel()
    val bindingUiState by bindingViewModel.bindingUiState.collectAsStateWithLifecycle()
    val bindingLogs by bindingViewModel.bindingLogs.collectAsStateWithLifecycle()

    when (val bindingState = bindingUiState.bindingState) {
        is UiState.Error -> {
            AlertDialogView(
                onDismiss = {
                    // Change state to idle.
                    bindingViewModel.updateBindingState(UiState.Idle())
                },
                onConfirm = {
                    // Retry binding. Set state to loading and call the binding function again.
                },
                title = "Binding Failed.",
                message = "Unable to bind the device, please try again.",
                confirmText = "Retry"
            )
        }

        is UiState.Idle -> {
            // Do nothing, show the normal UI.
        }

        is UiState.Loading -> {
            BindingLoaderDialog(bindingLogs) {
                // Text Content
                Text(
                    text = "Binding...",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.inverseOnSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = NordicSun
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Binding in progress, it might take few seconds. Please don't close the app.",
                        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }

        is UiState.Success -> {
            NordicLogger.info("Binding Success", tag = "Bindings")
            bindingViewModel.updateActiveBinding(bindingState.data)
            bindingViewModel.updateBindingState(UiState.Idle())
            showToast(
                message = "Binding completed successfully!",
                duration = ToastDuration.Long,
                gravity = ToastGravity.Center
            )
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .then(if (bindingUiState.bindingState is UiState.Loading) Modifier.cloudy() else Modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Concept Header
        item {
            OutlinedCard {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Transform,
                        contentDescription = "Bindings Explanation",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Understanding Matter Bindings",
                        )
                        Text(
                            text = "The Binding Cluster (0x001E) allows client nodes to directly control target servers over unicast/multicast fabric connections, bypassing intermediate bridge proxies entirely.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Configuration Section
        item {
            Text(
                text = "Write Matter Binding Cluster (0x001E)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            if (bindingUiState.sourceDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = "No eligible source devices found. Please add a Light Switch or Outlet device to configure bindings.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                return@item
            } else {
                BindingTableDetails(bindingUiState, {
                    bindingViewModel.onSourceSelected(it)

                }, { sourceId, targetId ->
                    bindingViewModel.initiateBinding(sourceId, targetId)
                })
            }
        }

        // Active Binding Lists
        item {
            Text(
                text = "Active Binding Table Entries (${bindingUiState.activeBindings.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (bindingUiState.activeBindings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = "No active bindings configured.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            // List active bindings
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    bindingUiState.activeBindings.forEach { binding ->
                        BindingCardRow(binding = binding)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BindingTableDetails(
    bindingScreenState: BindingUiState,
    onSourceSelected: (sourceDeviceId: DeviceId) -> Unit,
    initiateBinding: (sourceDeviceId: DeviceId, targetDeviceId: DeviceId) -> Unit,
) {
    var selectedTargetDevice by rememberSaveable { mutableStateOf<Long?>(null) }
    var isSourceDropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var isTargetDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    var sourceText by rememberSaveable { mutableStateOf("Select Light Switch") }
    var targetText by rememberSaveable { mutableStateOf("Select Light Bulb") }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selector for Source Node
            Column {
                Text(
                    text = "Select Client / Source Node (Write Client)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                ExposedDropdownMenuBox(
                    expanded = isSourceDropdownExpanded,
                    onExpandedChange = { isSourceDropdownExpanded = !isSourceDropdownExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {

                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSourceDropdownExpanded)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isSourceDropdownExpanded,
                        onDismissRequest = { isSourceDropdownExpanded = false }
                    ) {
                        bindingScreenState.sourceDevices.forEach { device ->
                            DropdownMenuItem(
                                text = {
                                    Text("${device.productName} (Node ID: ${device.deviceId.longValue})")
                                },
                                onClick = {
                                    sourceText =
                                        device.productName
                                            ?: "Node ${device.deviceId.longValue}"
                                    isSourceDropdownExpanded = false
                                    onSourceSelected(device.deviceId)
                                }
                            )
                        }
                    }
                }
            }

            if (bindingScreenState.selectedSourceDeviceId == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Please select a source device to see eligible target devices.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                return@Column
            } else if (bindingScreenState.eligibleTargetDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = "No eligible target devices found for the selected source. Please ensure you have a compatible Light or Dimmable light device added.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                return@Column
            } else {
                Column {
                    Text(
                        text = "Select Server / Target Node (Control Target)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    ExposedDropdownMenuBox(
                        expanded = isTargetDropdownExpanded,
                        onExpandedChange = { isTargetDropdownExpanded = !isTargetDropdownExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {

                        OutlinedTextField(
                            value = targetText,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTargetDropdownExpanded)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isTargetDropdownExpanded,
                            onDismissRequest = { isTargetDropdownExpanded = false }
                        ) {
                            bindingScreenState.eligibleTargetDevices.forEach { device ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${device.productName} (Node ID: ${device.deviceId.longValue})")
                                    },
                                    onClick = {
                                        targetText = device.productName
                                            ?: "Node ${device.deviceId.longValue}"
                                        isTargetDropdownExpanded = false
                                        selectedTargetDevice = device.deviceId.longValue
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Cluster Info static tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SwapCalls,
                    contentDescription = "Target Cluster",
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Target Action: Write Cluster 0x0006 (OnOff Bind Struct)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }


            Button(
                onClick = {
                    if (selectedTargetDevice != null) {
                        NordicLogger.debug(
                            "Initiating binding with source: ${bindingScreenState.selectedSourceDeviceId}, target: $selectedTargetDevice",
                            tag = "BindingScreen"
                        )
                        initiateBinding(
                            bindingScreenState.selectedSourceDeviceId,
                            (selectedTargetDevice as Long).toDeviceId(),
                        )
                    }

                },
                enabled = selectedTargetDevice != null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Write Binding ")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BindingTableDetailsPreview() {
    NordicTheme {
        BindingTableDetails(
            bindingScreenState = BindingUiState(
                sourceDevices = listOf(
                    DeviceTest_LIGHT
                ),
                eligibleTargetDevices = listOf(
                    DeviceTest_LIGHT
                )
            ),
            {},
        ) { _, _ -> }

    }
}

@Composable
fun BindingCardRow(
    binding: DeviceBinding
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),

        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = "Index",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Binding ID: ${binding.id}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Client ID: ${binding.sourceNodeId.longValue}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingFlat,
                        contentDescription = "links to",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = "Server ID: ${binding.targetNodeId.longValue}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Bound Cluster ID: 0x00${binding.clusterId}L (OnOff Cluster)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BindingCardRowPreview() {
    BindingCardRow(
        binding = DeviceBindingTest
    )
}
