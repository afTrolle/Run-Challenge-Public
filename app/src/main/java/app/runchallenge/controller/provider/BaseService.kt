package app.runchallenge.controller.provider

import androidx.lifecycle.LiveData




abstract class BaseService<T> : LiveData<T>() {


    //  private val mBehaviorSubject: BehaviorSubject<T> = BehaviorSubject.create()
    // private var disposableMap: Map<Any, Disposable> = mapOf()
//
//    protected fun onValue(t: T) {
//        mBehaviorSubject.onValue(t)
//    }
//
//    /**
//     * Called when going from zero to 1 subscribers
//     */
//    protected open fun onFirstSubscription() {
//        //optional to override  this
//    }
//
//    /**
//     * called when zero subscribers
//     */
//    protected open fun onZeroSubscribers() {
//        //optional to override  this
//    }
//
//    /**
//     * subscribe to Service
//     * onValue will be called with value
//     */
//    public fun subscribeToService(
//        eventHandler: (T) -> Unit
//    ) {
//        //If not identifier in map (subscribe)
//        if (!disposableMap.containsKey(eventHandler)) {
//
//            //subscribe to service
//            val subscribeReference = mBehaviorSubject.subscribe(eventHandler)
//
//            //going from 0 -> 1 subscriber
//            if (disposableMap.isEmpty()) {
//                onFirstSubscription()
//            }
//
//            disposableMap = disposableMap.plus(Pair(eventHandler, subscribeReference))
//        }
//    }
//
//    /**
//     * unSubscribe from service IE stop receiving events
//     */
//    public fun unSubsribeToService(eventHandler: (T) -> Unit) {
//
//        val subscriberDisposable = disposableMap[eventHandler]
//
//        //if not null
//        subscriberDisposable?.let {
//            disposableMap = disposableMap.minus(eventHandler)
//
//            //no more subscribers
//            if (disposableMap.isEmpty()) {
//                onZeroSubscribers()
//            }
//
//            //unSubscribe subscriber
//            if (!subscriberDisposable.isSubscribed) {
//                subscriberDisposable.unSubscribe()
//            }
//        }
//
//    }


}


