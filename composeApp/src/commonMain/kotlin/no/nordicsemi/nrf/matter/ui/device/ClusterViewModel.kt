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

/**
 * Holds the UI state of a single cluster of a device. Each subclass owns one cluster and exposes
 * its state as a [kotlinx.coroutines.flow.StateFlow], so that the UI stays stateless.
 */
sealed class ClusterViewModel(private val scope: CoroutineScope) {

    private val tag: String
        get() = this::class.simpleName ?: "ClusterViewModel"

    /** Reports every value of [source] to [onValue] until [scope] is cancelled. */
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

    /** Sends [command] to the device, calling [onFailure] when it could not be delivered. */
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

/** Creates the view model driving the UI of this cluster. */
fun Cluster.toViewModel(scope: CoroutineScope): ClusterViewModel = when (this) {
    is OnOffCluster -> OnOffViewModel(this, scope)
    is LevelControlCluster -> LevelControlViewModel(this, scope)
    is DoorLockCluster -> DoorLockViewModel(this, scope)
    is BasicInfoExtCluster -> BasicInfoExtViewModel(this, scope)
    is ManufacturerSpecCluster -> ManufacturerSpecViewModel(this, scope)
}
