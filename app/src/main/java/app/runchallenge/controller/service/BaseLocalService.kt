package app.runchallenge.controller.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ServiceCompat
import app.runchallenge.controller.provider.notification.MyNotification
import app.runchallenge.controller.provider.notification.MyNotificationImpl


abstract class BaseLocalService : Service() {
    private val mBinder = LocalBinder()
    private lateinit var myNotification: MyNotification

    override fun onBind(intent: Intent?): IBinder? {
        //return it self
        return mBinder
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService() = this@BaseLocalService
    }

    override fun onCreate() {
        super.onCreate()
        myNotification = MyNotificationImpl(this.applicationContext)
        myNotification.enableChannel()
        startForeground(myNotification.notificationId, myNotification.notification)
    }

    override fun onDestroy() {
        super.onDestroy()

        ServiceCompat.stopForeground(this, 0)
        myNotification.disableChannel()
    }

}