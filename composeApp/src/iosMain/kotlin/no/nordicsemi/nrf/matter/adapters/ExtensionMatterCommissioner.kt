@file:OptIn(ExperimentalForeignApi::class)

package no.nordicsemi.nrf.matter.adapters

import kotlinx.cinterop.ExperimentalForeignApi
import iosMatter.MatterCommissioner
import iosMatter.SwiftLogger

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
