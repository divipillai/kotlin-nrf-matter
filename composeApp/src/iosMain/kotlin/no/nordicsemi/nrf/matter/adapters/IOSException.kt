package no.nordicsemi.nrf.matter.adapters

import platform.Foundation.NSError

data class IOSException(val origin: NSError) : Throwable()
