@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId
import no.nordicsemi.nrf.matter.model.DeviceMatterInfo
import no.nordicsemi.nrf.matter.model.DeviceType
import no.nordicsemi.nrf.matter.model.ManufacturerSpecificData
import platform.Foundation.NSNumber

fun swiftPMImport.no.nordicsemi.nrf.matter.composeApp.Device.toDomain(): Device {
    return Device(
        deviceId = this.deviceId.toDeviceId(),
        dateCommissioned = this.dateCommissioned.longValue,
        vendorId = this.vendorId,
        productId = this.producId,
        deviceType = DeviceType.parse(this.deviceType.longValue),
        name = this.name,
        productName = this.productName,
        vendorName = this.vendorName,
        uniqueId = this.uniqueId,
        softwareVersion = this.softwareVersion,
        specificationVersion = this.specificationVersion.longValue,
        serialNumer = this.serialNumber,
        deviceMatterInfo = this.deviceMatterInfo
            .filterIsInstance<swiftPMImport.no.nordicsemi.nrf.matter.composeApp.DeviceMatterInfo>()
            .map { it.toDomain() }
    )
}

fun swiftPMImport.no.nordicsemi.nrf.matter.composeApp.DeviceMatterInfo.toDomain(): DeviceMatterInfo {
    return DeviceMatterInfo(
        endpoint = this.endpoint.intValue,
        types = this.types.filterIsInstance<NSNumber>().map { it.longValue },
        serverClusters = this.serverClusters.filterIsInstance<NSNumber>().map { it.longValue },
        clientClusters = this.clientClusters.filterIsInstance<NSNumber>().map { it.longValue },
        manufacturerSpecificData = this.manufacturerSpecificData?.toDomain()
    )
}

fun swiftPMImport.no.nordicsemi.nrf.matter.composeApp.ManufacturerSpecificData.toDomain(): ManufacturerSpecificData {
    return ManufacturerSpecificData(
        name = this.name,
        led = this.led,
        button = this.button,
    )
}

fun Long.toNSNumber() = NSNumber(long = this)

fun DeviceId.toNSNumber() = NSNumber(long = this.longValue)
fun Int.toNSNumber() = NSNumber(int = this)
fun NSNumber.toDeviceId() = DeviceId(this.stringValue)
