package no.nordicsemi.nrf.matter.binding

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import no.nordicsemi.nrf.matter.controller.BindingLogsProvider
import no.nordicsemi.nrf.matter.logger.NordicLogger

class BindingLogsProviderImpl: BindingLogsProvider {

    override val bindingLogs: Flow<String> = NordicLogger.logsChannel.consumeAsFlow()
}