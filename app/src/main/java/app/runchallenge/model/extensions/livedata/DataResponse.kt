package app.runchallenge.model.extensions.livedata

sealed class DataResponse<out T, out E> {
    data class Success<T>(val result: T) : DataResponse<T, Nothing>()
    object Working : DataResponse<Nothing, Nothing>()
    data class Error<E>(val error: E?) : DataResponse<Nothing, E>()
}

fun <E> DataResponse<Nothing, E>?.getIfError(): DataResponse.Error<E>? {
    return if (this is DataResponse.Error) {
        this
    } else {
        null
    }
}
