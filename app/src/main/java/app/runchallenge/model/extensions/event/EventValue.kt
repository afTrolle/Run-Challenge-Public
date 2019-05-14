package app.runchallenge.model.extensions.event

sealed class EventValue<out S, out E> {
    data class Success<out S>(val value: S) : EventValue<S, Nothing>()
    data class Error<out E>(val error: E) : EventValue<Nothing, E>()
}