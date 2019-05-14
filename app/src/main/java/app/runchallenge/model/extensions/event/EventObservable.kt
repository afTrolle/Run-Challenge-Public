package app.runchallenge.model.extensions.event

import app.runchallenge.model.extensions.runBlockingWith
import kotlinx.coroutines.sync.Mutex

/*  Thread safe observable calls all callbacks on same thread as set value function */
class EventObservable<S, E>(initialValue: S? = null) {

    //not guarantee that we have the right data when calling on Next
    // but we will eventually have the right amount of subscribers

    @Volatile
    private var callbacks = setOf<EventSubscription<S, E>>()
    //running lock so we don't lose any update of callbacks!
    private val mutex = Mutex()

    //not sure if need to be volatile
    @Volatile
    var value: EventValue<S, E>? = initialValue?.let { EventValue.Success(it) }

    fun addCallback(callback: EventSubscription<S, E>) {
        mutex.runBlockingWith {
            callbacks = callbacks.plus(callback)
        }
    }

    internal fun unSubscribe(callback: EventSubscription<S, E>) {
        mutex.runBlockingWith {
            callbacks = callbacks.minus(callback)
        }
    }

    fun onValue(next: S) {
        val callbacks = callbacks
        value = EventValue.Success(next)
        callbacks.forEach {
            it.onNext?.invoke(next)
        }
    }

    fun onError(error: E) {
        val callbacks = callbacks
        value = EventValue.Error(error)
        callbacks.forEach {
            it.onError?.invoke(error)
        }
    }

    fun subscribe(onNext: ((S) -> Unit)? = null, onError: ((E) -> Unit)? = null): EventSubscription<S, E> {
        val callback = EventSubscription(onNext, onError, this)
        addCallback(callback)
        return callback
    }


    //TODO that could be added is on which thread to call callback on
    //though just add launch at lambda would fix this
    //the risk is that we highjack thread
}

