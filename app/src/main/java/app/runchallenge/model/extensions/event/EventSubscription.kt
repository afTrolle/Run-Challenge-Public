package app.runchallenge.model.extensions.event

/**
 * subscription to event observable
 * */
data class EventSubscription<S, E>(
    internal val onNext: ((S) -> Unit)?,
    internal val onError: ((E) -> Unit)?,
    private val eventObservable: EventObservable<S, E>
) {
    @Volatile
    var isSubscribed = false
        private set

    fun unSubscribe() {
        eventObservable.unSubscribe(this)
        isSubscribed = true
    }

}