package no.nordicsemi.nrf.matter.model

import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.nrf.matter.matter.MatterDevicesProvider
import platform.Foundation.NSNumber
import platform.Matter.MTRBaseClusterDescriptor
import platform.Matter.MTRBaseClusterOnOff
import platform.Matter.MTRBaseDevice
import platform.Matter.MTRClusterDescriptor
import platform.Matter.MTRClusterLevelControl
import platform.Matter.MTRLevelControlClusterMoveParams
import platform.Matter.MTRLevelControlClusterMoveToLevelWithOnOffParams
import platform.Matter.MTRLevelControlClusterMoveWithOnOffParams
import platform.Matter.MTROnOffClusterToggleParams
import platform.Matter.MTRReadParams
import platform.Matter.create
import platform.darwin.dispatch_queue_create
import kotlin.coroutines.resumeWithException

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

class IosDeviceController: DeviceController {
    override suspend fun setDeviceOnOff(
        deviceId: Long,
        isDeviceOnline: Boolean,
        isOn: Boolean,
        endpoint: Int,
    ) {
        val endpoint = 13
        Napier.i("setDeviceOnOff - deviceId: $deviceId, isDeviceOnline: $isDeviceOnline, isOn: $isOn, endpoint: $endpoint")
        val device = MatterDevicesProvider.getDevice() ?: return
        val baseDevice = MTRBaseDevice.deviceWithNodeID(device.nodeID, device.deviceController!!)
//        val cluster = MTRClusterOnOff.create(
//            device = device,
//            endpointID = NSNumber(endpoint),
//            queue = dispatch_queue_create("no.nordicsemi.nrf.matter.clusteronoff", null)
//        )

        val baseCluster = MTRBaseClusterOnOff.create(
            device = baseDevice,
            endpointID = NSNumber(endpoint),
            queue = dispatch_queue_create("no.nordicsemi.nrf.matter.clusteronoff", null)
        )

        if (isOn) {
            suspendCancellableCoroutine { continuation ->
                baseCluster?.onWithCompletion {
                    continuation.resume(Unit) { cause, _, _ ->
                        // TODO
                    }
                }
            }
        } else {
            suspendCancellableCoroutine { continuation ->
                baseCluster?.offWithCompletion {
                    continuation.resume(Unit) { cause, _, _ ->
                        // TODO
                    }
                }
            }
        }
//
//        baseCluster!!.invokeCommandWithEndpointID(
////            endpoint = NSNumber(endpoint),
////            clusterID = NSNumber(endpoint),
//        )
//
//        val cluster = MTRClusterLevelControl.create(
//            device = device,
//            endpointID = NSNumber(endpoint),
//            queue = dispatch_queue_create("no.nordicsemi.nrf.matter.clusteronoff", null)
//        )
//
//        val params = MTROnOffClusterToggleParams()
//        val expectedValueInterval = NSNumber(5000)\
//
//        val descriptor = MTRClusterDescriptor.create(
//            device = device,
//            endpoint.toUShort(),
//            dispatch_queue_create("no.nordicsemi.nrf.matter.clusteronoff", null)
//        )
//
//        val readParams = MTRReadParams()
//        val partsList = descriptor!!.readAttributePartsListWithParams(readParams)
//        Napier.i("partsList: $partsList")
//
//        val serverParams = MTRReadParams()
//        val serverList = descriptor!!.readAttributeServerListWithParams(serverParams)
//        Napier.i("serverList: $serverList")
//
////        suspendCancellableCoroutine { continuation ->
////
////            descriptor!!.readAttributePartsListWithCompletion { list, error ->
////                Napier.i("Child endpoints: $list")
////                continuation.resume(list) { cause, _, _ ->
////                    // TODO
////                }
////            }
////        }
////
////        suspendCancellableCoroutine { continuation ->
////            descriptor!!.readAttributeServerListWithCompletion { list, error ->
////                Napier.i("Supported clusters on endpoint $list")
////                continuation.resume(list) { cause, _, _ ->
////                    // TODO
////                }
////            }
////        }
////        descriptor?.
////
////        descriptor.readAttributePartsList { values, error in
////            // tu masz listę endpointów
////        }
//
//        suspendCancellableCoroutine { continuation ->
//            val params = MTRLevelControlClusterMoveToLevelWithOnOffParams()
//            params.level = NSNumber(if (isOn) 254 else 0)
//
//            cluster!!.moveToLevelWithOnOffWithParams(params = params, expectedValues = null, expectedValueInterval = expectedValueInterval) {
//                if (it != null) {
//                    continuation.resumeWithException(Exception("Operation failed exception"))
//                } else {
//                    continuation.resume(value = Unit) { cause, _, _ ->
//                        // TODO
//                    }
//                }
//            }
//
////            cluster!!.toggleWithParams(params = params, expectedValues = null, expectedValueInterval = expectedValueInterval) {
////                if (it != null) {
////                    continuation.resumeWithException(Exception("Operation failed exception"))
////                } else {
////                    continuation.resume(value = Unit) { cause, _, _ ->
////                        // TODO
////                    }
////                }
////            }
//        }
    }

    override suspend fun unlinkDevice(deviceId: Long) {
        TODO("Not yet implemented")
    }
}
