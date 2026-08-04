package no.nordicsemi.nrf.matter.commission

import no.nordicsemi.nrf.matter.model.DeviceId
import platform.Foundation.NSError
import platform.Foundation.NSNumber

fun NSError.toCommissioningException(): CommissioningException? {
    val deviceIdNumber = userInfo["deviceId"] as? NSNumber ?: return null
    val stageRawValue = userInfo["stage"] as? Int ?: return null
    val displayMsg = userInfo["displayMessage"] as? String ?: return null
    val fabricId = userInfo["fabricId"] as? Int ?: return null

    return CommissioningException(
        deviceId = DeviceId(deviceIdNumber.stringValue),
        stage = Stage.entries[stageRawValue],
        errorCode = code.toInt(),
        displayMessage = displayMsg,
        fabricId = fabricId
    )
}
