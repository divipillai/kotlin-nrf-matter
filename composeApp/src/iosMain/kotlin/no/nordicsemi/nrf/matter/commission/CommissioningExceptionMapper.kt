package no.nordicsemi.nrf.matter.commission

import no.nordicsemi.nrf.matter.model.DeviceId
import platform.Foundation.NSError

fun NSError.toCommissioningException(): CommissioningException? {
    val deviceIdNumber = userInfo["deviceId"] as? Long ?: return null
    val stageRawValue = userInfo["stage"] as? Long ?: return null
    val displayMsg = userInfo["displayMessage"] as? String ?: return null
    val fabricId = userInfo["fabricId"] as? Long ?: return null

    return CommissioningException(
        deviceId = DeviceId(deviceIdNumber.toString()),
        stage = Stage.entries[stageRawValue.toInt()],
        errorCode = code.toInt(),
        displayMessage = displayMsg,
        fabricId = fabricId.toInt()
    )
}
