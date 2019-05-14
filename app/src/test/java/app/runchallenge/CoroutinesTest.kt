package app.runchallenge

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.runchallenge.view.fragment.base.BaseViewModel
import com.google.android.gms.common.internal.Asserts
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.Exception

class CoroutinesTest {


    /**
     * TODO what to learn
     *
     * //cancellation, cancel running task
     * //exception handling, dealing with an exception
     *
     *
     * //context, context is on which thread pool to use.
     * //Scopes, which scope to use for what task,
     *
     * // Wrapping callbacks
     *
     * //launching , await
     *
     */

    /*
    Thread locking

    Fine grain, lock on the variable you are changing, prone to deadlocks maybe, at least prone to lost updates,
    Ui-thread good example

    Course grain, locks on big updates so performed usually do parallel tasks and aggregate result then update value
    (run it's own single thread in that part of the code no worries about concurenecy)

    */

    /*
    Note to self
    if ViewModel is onCleared
     */


    @Test
    fun testCancelRunningJob() {
        var test: Int = 1

        runBlocking {
            val job = testScope.launch {
                try {
                    testJob.cancel()
                    delay(20)
                    test = 2
                } finally {
                    //clean up
                }
            }
            //wait for completion
            job.join()
        }

        Assertions.assertEquals(1, test)
    }

    @Test
    fun testIgnoreCancelRunningJob() {
        var test: Int = 1
        runBlocking {
            val job = testScope.launch {
                try {
                    withContext(NonCancellable) {
                        testJob.cancel()
                        delay(100)
                        test = 2
                    }
                } finally {
                    //clean up
                }
            }
            //wait for completion
            job.join()
        }

        Assertions.assertEquals(2, test)
    }

    private val testJob = Job()
    private val testScope = CoroutineScope(Dispatchers.IO + testJob)


    suspend fun test(delay: Long) {

        try {
            delay(delay)

        } finally {
            //clean up (CancellationException)

        }
    }


    /*
    * isn't really needed though,
    *
    * we use ViewModelScope already created
    **/

    //used to cancel all running jobs in scope
    private val viewModelJob = Job()

    //A scope controls the lifetime of coroutines through its job.
    // When you cancel the job of a scope, it cancels all coroutines started in that scope.
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    //maybe needed inside services, for threading. (synronization)
    @ObsoleteCoroutinesApi
    val singleThreadContext = newSingleThreadContext("poolname")


    fun launchTest() {

        //call parallel tasks
        val test = runBlocking {

            //parallel task
            val one = async {
                delay(100)
                return@async 1
            }

            //parallel task
            val two = async {
                delay(100)
                return@async 1
            }

            return@runBlocking one.await() + two.await()
        }

        //fire and forget
        GlobalScope.launch {
            //parallel task
            val one = async {
                delay(100)
                return@async 1
            }

            //parallel task
            val two = async {
                delay(100)
                return@async 1
            }
        }

        //co routines to call services,
        //they respond by updating livedata

        //that way it's decoupled
        // fire and forget, because result will be given through the observable.

        //wanna do launch at start but while inside services it's fine to use async

        fun BaseViewModel.launch(f: () -> Unit): Job {
            return viewModelScope.launch {
                f()
            }
        }

        fun <T> BaseViewModel.async(f: () -> T): Deferred<T> {
            return viewModelScope.async {
                return@async f()
            }
        }

    }


    /* higher order retry*/

    suspend fun <T> retryIO(block: suspend () -> T): T {
        var delay = 100L
        while (true) {
            try {
                return block()
            } catch (exception: Exception) {

            }
            delay(delay)
            delay = (delay * 2).coerceAtMost(5000L)
        }
    }

    /* coroutine builders*/

    fun testEmpty() {
        runBlocking {
            //returns immedaitly
            launch(Dispatchers.Main) {


            }
        }
    }


    /*  async */

    suspend fun loadAsync(): Deferred<Unit> {
        return withContext(Dispatchers.IO) {
            async {

            }
        }
    }

    suspend fun load() {

    }

}