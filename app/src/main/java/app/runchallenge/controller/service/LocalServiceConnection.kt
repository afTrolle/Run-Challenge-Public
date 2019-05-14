package app.runchallenge.controller.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

//called Start service from outside
sealed class MyServiceBinding {
    class Connected(val service: BaseLocalService) : MyServiceBinding()
    object NotConnected : MyServiceBinding()
}

/**
 * For this to run service in foreground we make implicit promise too call
 * startForeground(int, android.app.Notification) inside the service also
 */
fun <T> Context.startForegroundLocalService(serviceJavaClass: Class<T>): LiveData<MyServiceBinding> {
    val intent = Intent(this, serviceJavaClass)

    //start indefinite service, required to get service into foreground value
    ContextCompat.startForegroundService(this, intent)

    val connectionLiveData =
        MutableLiveData<MyServiceBinding>(MyServiceBinding.NotConnected)

    val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            connectionLiveData.value = MyServiceBinding.NotConnected
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is BaseLocalService.LocalBinder) {
                connectionLiveData.value = MyServiceBinding.Connected(service.getService())
            }
        }
    }

    //bind to service, means that we need to
    bindService(intent, serviceConnection, 0)

    return connectionLiveData
}

fun <T> Context.startForegroundLocalService(serviceJavaClass: Class<T>, serviceConnection: ServiceConnection): Boolean {
    val intent = Intent(this, serviceJavaClass)

    //start indefinite service, required to get service into foreground value
    ContextCompat.startForegroundService(this, intent)

    //bind to service, means that we need to
    return bindService(intent, serviceConnection, 0)
}

fun <T> Context.stopForegroundLocalService(serviceJavaClass: Class<T>): Boolean {
    val intent = Intent(this, serviceJavaClass)
    return stopService(intent)
}


/*stops service if bound*/
fun LiveData<MyServiceBinding>.stopService() {
    val currentValue = value
    if (currentValue is MyServiceBinding.Connected) {
        //might not work TODO check if works
        currentValue.service.stopSelf()
    }
}

