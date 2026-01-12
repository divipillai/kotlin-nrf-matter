package no.nordicsemi.nrf.matter.navigation

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import no.nordicsemi.nrf.matter.home.BackgroundDark
import no.nordicsemi.nrf.matter.home.BackgroundLight
import no.nordicsemi.nrf.matter.home.HomeViewModel
import no.nordicsemi.nrf.matter.home.Primary
import no.nordicsemi.nrf.matter.home.commissionDevice
import org.koin.androidx.compose.koinViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationLayout(navController: NavHostController) {
    // TODO: There must be a better way to allow child composable to easily update the
    // TopAppBar title of a shared scaffold.
    // The way it is done here, the lambda updateTopAppBarTitle must be passed to all
    // the routes. Lots of boilerplate code needed.
    // Have not been able to make it work with a shared AppViewModel.
    // val topAppBarTitle by appViewModel.topAppBarTitle.collectAsState()
    var topAppBarTitle by rememberSaveable { mutableStateOf("nRF Matter") }

    val updateTopAppBarTitle: (title: String) -> Unit = remember {
        { title ->
            topAppBarTitle = title
        }
    }
    val homeViewModel: HomeViewModel = koinViewModel()
    // UI Model for all the devices shown on the screen.
    val devicesUiModel by homeViewModel.devicesUiModelLiveData.observeAsState()
    val devices = devicesUiModel?.devices
    val devicesList = devices ?: emptyList()

    val isDark = isSystemInDarkTheme()

    val commissionDeviceLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            val resultCode = result.resultCode
            if (resultCode == Activity.RESULT_OK) {
                Log.d("AAA", "CommissionDevice: Success")
                // We let the ViewModel know that GPS commissioning has completed successfully.
                // The ViewModel knows that we still need to capture the device name and will\
                // update UI state to trigger the NewDeviceAlertDialog.
                homeViewModel.gpsCommissioningDeviceSucceeded(result)
            } else {
                homeViewModel.commissionDeviceFailed(resultCode)
            }
        }
    val context = LocalContext.current
    val onCommissionDevice: () -> Unit = remember {
        {
            Log.d("AAA", "onAddDeviceClick")
            // fixme deviceAttestationFailureIgnored = false
            commissionDevice(context.applicationContext, commissionDeviceLauncher)
        }
    }

    Scaffold(
        containerColor = if (isDark) BackgroundDark else BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = topAppBarTitle,

                            color = if (isSystemInDarkTheme()) Color.White else Primary)
                    }
                },

                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.Filled.Home, contentDescription = "Home",
                            tint = if (isSystemInDarkTheme()) Color.White else Primary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO() */ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings",
                            tint = if (isSystemInDarkTheme()) Color.White else Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                )
            )
        },
        floatingActionButton = {
            // Only show FAB if we already have devices
            if (devicesList.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onCommissionDevice() },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(navController, innerPadding, updateTopAppBarTitle, onCommissionDevice)
    }
}