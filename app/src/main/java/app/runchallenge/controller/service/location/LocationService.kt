package app.runchallenge.controller.service.location

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import app.runchallenge.controller.provider.location.LocationProviderImpl
import app.runchallenge.controller.service.BaseLocalService
import app.runchallenge.controller.service.startForegroundLocalService
import app.runchallenge.controller.service.stopForegroundLocalService
import app.runchallenge.view.application.MyApplication

class LocationService : BaseLocalService() {

    companion object Factory {
        fun startService(context: Context, callback: (location: LocationService?) -> Unit): Boolean {

            val serviceConnection = object : ServiceConnection {
                override fun onServiceDisconnected(name: ComponentName?) {
                    callback(null)
                }

                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    if (service is LocalBinder) {
                        val localService = service.getService()
                        if (localService is LocationService) {
                            callback(localService)
                        }
                    }
                }
            }
            return context.startForegroundLocalService(LocationService::class.java, serviceConnection)
        }

        fun stopService(context: Context): Boolean {
            return context.stopForegroundLocalService(LocationService::class.java)
        }
    }

    lateinit var locationProviderImpl: LocationProviderImpl

    override fun onCreate() {
        super.onCreate()
        val application = application
        if (application is MyApplication) {
            locationProviderImpl = LocationProviderImpl(applicationContext, application.locationReqest)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationProviderImpl.startLocationProvider()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }


    override fun onDestroy() {
        locationProviderImpl.stopLocationProvider()
        super.onDestroy()
    }

}