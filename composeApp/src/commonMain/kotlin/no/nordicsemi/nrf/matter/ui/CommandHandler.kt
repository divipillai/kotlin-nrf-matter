package no.nordicsemi.nrf.matter.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import no.nordicsemi.nrf.matter.device.UiState
import no.nordicsemi.nrf.matter.model.Device
import no.nordicsemi.nrf.matter.model.DeviceId

interface CommandHandler {
    fun resolveEndpoint(device: Device, clusterId: Long): Int {
        return device.deviceMatterInfo
            .firstOrNull { it.serverClusters.contains(clusterId) }
            ?.endpoint ?: 0 // TODO: change to exception and handle from UI.
    }

    fun <T> Flow<T>.withUiState(): Flow<UiState<T>> {
        return this
            .map<T, UiState<T>> { UiState.Success(it) }
            .onStart { emit(UiState.Loading()) }
            .catch { emit(UiState.Error("Error during executing operation.", it)) }
    }

    fun <T> withUiState(block: suspend () -> T): Flow<UiState<T>> {
        return flow {
            try {
                emit(UiState.Loading())
                emit(UiState.Success(block()))
            } catch (t: Throwable) {
                t.printStackTrace()
                emit(UiState.Error("Error during executing operation.", t))
            }
        }
    }
}
