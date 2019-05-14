package app.runchallenge.model.extensions.state

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MyState<T>(initialState: T) {

    private val mutex = Mutex()

    //Might be overkill to have volatile
    //otherwise we need to do a blocking get value
    @Volatile
    var value = initialState
        private set

    /**
     *  Used to update internal state where we need locking  Ie Read and write to state
     */
    suspend fun updateState(criticalSection: suspend (state: T) -> T) {
        mutex.withLock {
            value = criticalSection(value)
        }
    }


    /**
     * blocking read of state
     */
    suspend fun getStateBlocking(): T {
        return mutex.withLock { value }
    }

}
