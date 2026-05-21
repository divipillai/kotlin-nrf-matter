package no.nordicsemi.nrf.matter.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.screens.DeviceItemContainer
import nrfmatterformobile.composeapp.generated.resources.Res
import nrfmatterformobile.composeapp.generated.resources.smart_outlet
import org.jetbrains.compose.resources.painterResource

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
internal fun SwitchItem(
    deviceId: DeviceId,
    title: String,
    subtitle: String,
    onDeviceClick: () -> Unit,
) {

    DeviceItemContainer(
        icon = painterResource(Res.drawable.smart_outlet),
        title = title,
        subtitle = subtitle,
        isOnline = false,
        onDeviceClick = onDeviceClick
    ) {}
}

@Preview
@Composable
private fun SwitchItem_Preview() {
    SwitchItem(
        deviceId = DeviceId.Zero,
        title = "Light switch",
        subtitle = "Bind the device with other devices ",
        onDeviceClick = {}
    )
}
