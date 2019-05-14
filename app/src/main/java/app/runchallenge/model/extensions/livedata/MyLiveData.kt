package app.runchallenge.model.extensions.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import app.runchallenge.model.data.room.RoomState
import app.runchallenge.model.extensions.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

open class MyLiveData<T : Any?, E : Any?> {

    private val liveData = InnerLiveData()

    open var default: T? = null

    private val observeForeverMap = mutableMapOf<Any, Observer<DataResponse<T, E>>>()

    private inner class InnerLiveData : MediatorLiveData<DataResponse<T, E>>() {

        override fun onActive() {
            super.onActive()
            this@MyLiveData.onActive()
        }

        override fun onInactive() {
            super.onInactive()
            this@MyLiveData.onInactive()
        }


        fun <SO : Any?, E0 : Any?> addSource(
            source: MyLiveData<SO, E0>,
            onChange: (data: SO) -> Unit,
            onError: (error: E0?) -> Unit
        ) {
            addSource(source.liveData) { response ->
                when (response) {
                    is DataResponse.Success -> onChange(response.result)
                    is DataResponse.Error -> onError(response.error)
                }
            }
        }


        fun swapValue(t: T) {
            swapValue(DataResponse.Success(t))
        }

        fun swapValue(newResponse: DataResponse<T, E>?) {
            val oldValue = value
            if (newResponse != oldValue) {
                value = newResponse
            }
        }

    }

    /*public functions */

    fun observer(owner: LifecycleOwner, onChange: (data: T) -> Unit, onError: (error: E?) -> Unit) {
        liveData.observe(owner, Observer { response ->
            when (response) {
                is DataResponse.Success -> onChange(response.result)
                is DataResponse.Error -> onError(response.error)
            }
        })
    }


    fun observerForever(key: Any, onChange: (data: T) -> Unit, onError: (error: E?) -> Unit) {
        if (observeForeverMap.containsKey(key)) {
            return
        }
        val mObserver = Observer<DataResponse<T, E>> { response ->
            when (response) {
                is DataResponse.Success -> onChange(response.result)
                is DataResponse.Error -> onError(response.error)
            }
        }
        observeForeverMap[key] = mObserver
        liveData.observeForever(mObserver)
    }

    fun removeObserver(key: Any) {
        val maybeObserver = observeForeverMap.remove(key)
        maybeObserver?.let {
            liveData.removeObserver(maybeObserver)
        }
    }


    fun <SO : Any?, E0 : Any?> addSource(
        source: MyLiveData<SO, E0>,
        onChange: (data: SO) -> Unit,
        onError: (error: E0?) -> Unit
    ) {
        liveData.addSource(source, onChange, onError)
    }


    /**
     * fetch current value if is a success item
     */
    val success: T?
        get() = liveData.value.let {
            if (it is DataResponse.Success) {
                it.result
            } else {
                null
            }
        }


    val error: E?
        get() = liveData.value.let {
            if (it is DataResponse.Error) {
                it.error
            } else {
                null
            }
        }

    val value: DataResponse<T, E>? get() = liveData.value

    val isWorking: Boolean = value is DataResponse.Working


    /* functions that can be overwrtiten */

    open fun onActive() {

    }

    open fun onInactive() {

    }


    /**
     * alert observers with value
     **/
    fun setForceValue(update: (oldValue: T?) -> T) {
        val newValue = update(success)
        liveData.swapValue(newValue)
    }


    /**
     * update data only if new data is provided
     **/
    fun setValue(update: (oldValue: T?) -> T) {
        val newValue = update(success)
        liveData.swapValue(newValue)
    }


    /**
     * update data only if new data is provided if no existing success value exists  get default
     **/
    fun setDefaultValue(update: (oldValue: T) -> T) {
        val prevState = success ?: default ?: throw Exception("Missing default value declared/implemented")
        val newValue = update(prevState)
        liveData.swapValue(newValue)
    }


    /**
     * alert observers of value update if new value is given
     */
    fun setSuccess(t: T) {
        liveData.swapValue(DataResponse.Success(t))
    }

    /**
     * alert observers of value update
     */
    fun setForceSuccess(t: T) {
        liveData.value = DataResponse.Success(t)
    }

    /**
     * alert observers of  working value update if new value changed
     */
    fun setWorking() {
        liveData.swapValue(DataResponse.Working)
    }

    /**
     * alert observers of working value update
     */
    fun setForceWorking() {
        liveData.value = DataResponse.Working
    }

    /**
     * alert observers of working ignoreError update if new value
     */
    fun setError(e: E?) {
        liveData.swapValue(DataResponse.Error(e))
    }

    fun postError(e: E?) {
        liveData.postValue(DataResponse.Error(e))
    }

    /**
     * alert observers of working ignoreError value
     */
    fun setForceError(e: E?) {
        liveData.value = DataResponse.Error(e)
    }


    /*
    * Add source functions
    *
    **/
    fun <S1 : Any?, E1 : Any?, S2 : Any?, E2 : Any?> addMergeSource(
        source1: MyLiveData<S1, E1>,
        source2: MyLiveData<S2, E2>,
        merge: (S1, S2) -> Unit,
        onError: (sourceOneError: E1?, sourceTwoError: E2?) -> Unit
    ) {

        fun onChange(latestS1: S1?, latestS2: S2?) {
            val success1: S1? = latestS1 ?: source1.success
            val success2: S2? = latestS2 ?: source2.success
            if (success1 != null && success2 != null) {
                merge(success1, success2)
            }
        }

        liveData.addSource(source1, {
            onChange(it, null)
        }, {
            onError(it, null)
        })

        liveData.addSource(source2, {
            onChange(null, it)
        }, {
            onError(null, it)
        })
    }


    fun <S1 : Any?, E1 : Any?, S2 : Any?, E2 : Any?, S3 : Any?, E3 : Any?> addMergeSource(
        source1: MyLiveData<S1, E1>,
        source2: MyLiveData<S2, E2>,
        source3: MyLiveData<S3, E3>,
        merge: (S1, S2, S3) -> Unit,
        onError: (sourceOneError: E1?, sourceTwoError: E2?, sourceThreeError: E3?) -> Unit
    ) {
        fun onChange(latestS1: S1?, latestS2: S2?, latestS3: S3?) {
            val success1: S1? = latestS1 ?: source1.success
            val success2: S2? = latestS2 ?: source2.success
            val success3: S3? = latestS3 ?: source3.success
            if (success1 != null && success2 != null && success3 != null) {
                log("on merge called")
                merge(success1, success2, success3)
            }
        }

        liveData.addSource(source1, {
            onChange(it, null, null)
        }, {
            onError(it, null, null)
        })

        liveData.addSource(source2, {
            onChange(null, it, null)
        }, {
            onError(null, it, null)
        })

        liveData.addSource(source3, {
            onChange(null, null, it)
        }, {
            onError(null, null, it)
        })
    }

    fun postValue(function: () -> T) {
        liveData.postValue(DataResponse.Success(function()))
    }


    suspend fun setErrorAsync(oldValue: E?) {
        withContext(Dispatchers.Main) {
            setError(oldValue)
        }
    }

    suspend fun setValueAsync(update: (oldValue: T?) -> T) {
        withContext(Dispatchers.Main) {
            setValue(update)
        }
    }

}