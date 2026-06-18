package no.nordicsemi.nrf.matter.device

sealed interface OperationResult<T> {

    data class Success<T>(val data: T) : OperationResult<T>

    data class Error(
        val t: Throwable? = null
    ) : OperationResult<Nothing>
}