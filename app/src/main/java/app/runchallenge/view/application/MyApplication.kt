package app.runchallenge.view.application

import android.app.Application
import app.runchallenge.dagger.ApplicationModule
import app.runchallenge.dagger.DaggerApplicationComponent
import app.runchallenge.controller.repository.SettingsRepository
import com.google.android.gms.location.LocationRequest
import javax.inject.Inject


class MyApplication : Application() {
    //Global value of application

    val applicationModule = ApplicationModule(this)
    val applicationComponent = DaggerApplicationComponent.builder().applicationModule(applicationModule).build()


    @Inject
    lateinit var locationReqest: LocationRequest

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        applicationComponent.inject(this)


    }


}