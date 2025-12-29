package no.nordicsemi.nrf.matter.chip

import android.util.Log
import chip.devicecontroller.ChipDeviceController

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
/**
 * ChipDeviceController uses a CompletionListener for callbacks. This is a "base" default
 * implementation for that CompletionListener.
 */
abstract class BaseCompletionListener : ChipDeviceController.CompletionListener {
    override fun onConnectDeviceComplete() {
        Log.d("AAA","BaseCompletionListener onConnectDeviceComplete()")
    }

    override fun onStatusUpdate(status: Int) {
        Log.d("AAA","BaseCompletionListener onStatusUpdate(): status [${status}]")
    }

    override fun onPairingComplete(code: Int) {
        Log.d("AAA","BaseCompletionListener onPairingComplete(): code [${code}]")
    }

    override fun onPairingDeleted(code: Int) {
        Log.d("AAA","BaseCompletionListener onPairingDeleted(): code [${code}]")
    }

    override fun onCommissioningComplete(nodeId: Long, errorCode: Int) {
        Log.d("AAA",
            "BaseCompletionListener onCommissioningComplete(): nodeId [${nodeId}] errorCode [${errorCode}]"
        )
    }

    override fun onNotifyChipConnectionClosed() {
        Log.d("AAA","BaseCompletionListener onNotifyChipConnectionClosed()")
    }

    override fun onCloseBleComplete() {
        Log.d("AAA","BaseCompletionListener onCloseBleComplete()")
    }

    override fun onError(error: Throwable) {
        Log.d("AAA", "BaseCompletionListener onError()", error)
    }

    override fun onOpCSRGenerationComplete(csr: ByteArray) {
        Log.d("AAA","BaseCompletionListener onOpCSRGenerationComplete() csr [${csr}]")
    }

    override fun onReadCommissioningInfo(
        vendorId: Int,
        productId: Int,
        wifiEndpointId: Int,
        threadEndpointId: Int
    ) {
        Log.d("AAA",
            "onReadCommissioningInfo: vendorId [${vendorId}]  productId [${productId}]  wifiEndpointId [${wifiEndpointId}] threadEndpointId [${threadEndpointId}]"
        )
    }

    override fun onCommissioningStatusUpdate(nodeId: Long, stage: String?, errorCode: Int) {
        Log.d("AAA",
            "onCommissioningStatusUpdate nodeId [${nodeId}]  stage [${stage}]  errorCode [${errorCode}]"
        )
    }
}