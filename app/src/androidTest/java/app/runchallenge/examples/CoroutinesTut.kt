package app.runchallenge.examples

//import kotlinx.coroutines.*
//
//fun main() {
//
//    //exampleBlocking()
//    //exampleBlockingCo()
//    //dispatcherExample()
//
//    globalLaunch()
//}
//
//fun printDelayed(message: String) {
//    Thread.sleep(1000)
//    println(message)
//}
//
//suspend fun printDelayedCo(message: String) {
//    delay(1000)
//    println(message)
//}
//
//
//fun exampleBlocking() {
//    printDelayed("hello")
//    printDelayed("world")
//}
//
//fun exampleBlockingCo() {
//    runBlocking {
//        printDelayedCo("hello")
//        printDelayedCo("world")
//    }
//}
//
//fun exampleBlockingCoTwo() = runBlocking {
//    printDelayedCo("hello")
//    printDelayedCo("world")
//}
//
//fun dispatcherExample() {
//    //block main thread until block completed
//    // call from same worker
//    runBlocking(Dispatchers.Default) {
//        println("hello")
//        printDelayedCo("world")
//    }
//    println("!!!")
//}
//
//fun globalLaunch() = runBlocking {
//
//    //not blocking
//    //UI dispatcher
//    launch(Dispatchers.Main) {
//        printDelayedCo("hello from global ${Thread.currentThread().name}")
//    }
//
//    print("hello from local")
//}
//
//suspend fun longRunningTask(startNum: Int): Int {
//    delay(1000)
//    return startNum * 2
//}
//
//fun getInto() = runBlocking {
//    val deferredvalueOne = async { longRunningTask(1) }
//    val deferredvalueTwo = async { longRunningTask(1) }
//
//
//    val sum = deferredvalueOne.await() + deferredvalueTwo.await()
//}
//
//
//fun getIntoWithContext() = runBlocking {
//    val deferredvalueOne = withContext(Dispatchers.Default) { longRunningTask(1) }
//    val deferredvalueTwo = withContext(Dispatchers.Default) { longRunningTask(1) }
//
//
//    val sum = deferredvalueOne + deferredvalueTwo
//
//
//    runBlocking {
//        //return true if job is still active
//        isActive
//    }
//}