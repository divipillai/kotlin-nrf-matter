@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.no.nordicsemi.nrf.matter.composeApp.MatterCommissioner
import swiftPMImport.no.nordicsemi.nrf.matter.composeApp.SwiftLogger

class ExtensionMatterCommissioner {

    private val commissioner = MatterCommissioner()

    suspend fun commission() {
//        commissioner.commissionWithPayload()
        SwiftLogger.infoWithTag("s", "mine log")
    }

    fun releaseCommissioner() {
        commissioner.releaseCommissioner()
    }
}
