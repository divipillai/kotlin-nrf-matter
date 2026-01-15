package no.nordicsemi.nrf.matter.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import no.nordicsemi.nrf.matter.home.HomeViewModel
import no.nordicsemi.nrf.matter.home.commissionDevice
import no.nordicsemi.nrf.matter.ui.TitleAppBar
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
    var topAppBarTitle by rememberSaveable { mutableStateOf("nRF Matter") }
    val updateTopAppBarTitle: (title: String) -> Unit = remember {
        { topAppBarTitle = it }
    }
    val homeViewModel: HomeViewModel = koinViewModel()
    val devicesUiModel by homeViewModel.devicesUiModelLiveData.observeAsState()
    val devices = devicesUiModel?.devices
    val devicesList = devices ?: emptyList()

    val commissionDeviceLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            val resultCode = result.resultCode
            if (resultCode == Activity.RESULT_OK) {
                homeViewModel.gpsCommissioningDeviceSucceeded(result)
            } else {
                homeViewModel.commissionDeviceFailed(resultCode)
            }
        }
    val context = LocalContext.current
    val onCommissionDevice: () -> Unit = remember {
        {
            // fixme deviceAttestationFailureIgnored = false
            commissionDevice(context.applicationContext, commissionDeviceLauncher)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.displayCutout
            .only(WindowInsetsSides.Horizontal)
            .union(WindowInsets.navigationBars),
        topBar = {
            TitleAppBar(topAppBarTitle, navController) {
                /* TODO(): Implement settings click action */
            }
        },
        floatingActionButton = {
            // Only show FAB if we already have devices
            if (devicesList.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onCommissionDevice() },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(navController, innerPadding, updateTopAppBarTitle, onCommissionDevice)
    }
}