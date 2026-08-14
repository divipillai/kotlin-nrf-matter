package no.nordicsemi.nrf.matter.ui.device

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import no.nordicsemi.nrf.matter.cluster.BasicInfoExtCluster
import no.nordicsemi.nrf.matter.cluster.Cluster
import no.nordicsemi.nrf.matter.cluster.DoorLockCluster
import no.nordicsemi.nrf.matter.cluster.LevelControlCluster
import no.nordicsemi.nrf.matter.cluster.ManufacturerSpecCluster
import no.nordicsemi.nrf.matter.cluster.OnOffCluster
import no.nordicsemi.nrf.matter.logger.NordicLogger
import no.nordicsemi.nrf.matter.ui.infoext.BasicInfoExtViewModel
import no.nordicsemi.nrf.matter.ui.level.LevelControlViewModel
import no.nordicsemi.nrf.matter.ui.light.OnOffViewModel
import no.nordicsemi.nrf.matter.ui.lock.DoorLockViewModel
import no.nordicsemi.nrf.matter.ui.manspec.ManufacturerSpecViewModel

abstract class ClusterViewModel(private val scope: CoroutineScope) {

    private val tag: String
        get() = this::class.simpleName ?: "ClusterViewModel"

    protected fun <T> observe(source: suspend () -> Flow<T>, onValue: (T) -> Unit) {
        scope.launch {
            flow { emitAll(source()) }
                .catch { NordicLogger.error("Failed to observe the device.", it, tag = tag) }
                .collect { value ->
                    NordicLogger.info("New value: $value", tag = tag)
                    onValue(value)
                }
        }
    }

    protected fun send(onFailure: () -> Unit = {}, command: suspend () -> Unit) {
        scope.launch {
            try {
                command()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                NordicLogger.error("Failed to send a command to the device.", t, tag = tag)
                onFailure()
            }
        }
    }
}

fun Cluster.toViewModel(scope: CoroutineScope): ClusterViewModel = when (this) {
    is OnOffCluster -> OnOffViewModel(this, scope)
    is LevelControlCluster -> LevelControlViewModel(this, scope)
    is DoorLockCluster -> DoorLockViewModel(this, scope)
    is BasicInfoExtCluster -> BasicInfoExtViewModel(this, scope)
    is ManufacturerSpecCluster -> ManufacturerSpecViewModel(this, scope)
}
