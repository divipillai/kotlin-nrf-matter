@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.no.nordicsemi.nrf.matter.composeApp.MatterCommissioner

class ExtensionMatterCommissioner {

    private val commissioner = MatterCommissioner()

    suspend fun commission() {
//        commissioner.commissionWithPayload()

    }

    fun releaseCommissioner() {
        commissioner.releaseCommissioner()
    }
}
