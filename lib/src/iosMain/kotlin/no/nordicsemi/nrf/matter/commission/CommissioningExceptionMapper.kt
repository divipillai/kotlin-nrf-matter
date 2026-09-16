package no.nordicsemi.nrf.matter.commission

import no.nordicsemi.nrf.matter.model.DeviceId
import platform.Foundation.NSError

/**
 * Rebuilds the [CommissioningException] that `ios-matter` encoded into this error's `userInfo`.
 *
 * The keys are written by `NSError.withMoreUserInfo(deviceId:stage:displayMessage:)` in
 * `LocalMatterCommissioner.swift`. All four are required: an error that did not pass through there
 * — anything thrown straight out of the Matter framework, for instance — yields `null`, and callers
 * fall back to a generic failure.
 *
 * [stage] is looked up by ordinal, so [Stage] must stay aligned with the Swift `CommissioningStage`
 * enum.
 */
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
