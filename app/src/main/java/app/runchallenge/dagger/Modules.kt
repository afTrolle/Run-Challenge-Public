package app.runchallenge.dagger

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.runchallenge.controller.repository.game.GameRepositoryImpl
import app.runchallenge.controller.repository.SettingsRepository
import app.runchallenge.controller.repository.UserRepository
import app.runchallenge.controller.provider.AccountService
import app.runchallenge.controller.provider.LocationSettingsService
import app.runchallenge.controller.provider.PermissionsService
import app.runchallenge.controller.provider.SettingService
import app.runchallenge.controller.provider.location.LocationProvider
import app.runchallenge.controller.provider.location.LocationProviderImpl
import app.runchallenge.controller.provider.network.room.MyRoom
import app.runchallenge.controller.provider.network.room.MyRoomImpl
import app.runchallenge.view.application.MyApplication
import com.google.android.gms.location.LocationRequest
import dagger.Module
import dagger.Provides


@Module
class ApplicationModule(private val myApplication: MyApplication) {

    @Provides
    @ApplicationScope

    fun provideContext(): Context = myApplication.applicationContext

    @Provides
    @ApplicationScope
    fun provideAccountService(context: Context): AccountService {
        return AccountService(context)
    }

    @Provides
    @ApplicationScope
    fun provideSettings(context: Context): SettingService =
        SettingService(context)


    @Provides
    @ApplicationScope
    fun locationRequest(): LocationRequest =
        LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(500)
            // .setFastestInterval()
            .setMaxWaitTime(2000)
            .setSmallestDisplacement(0.5f) //0.5 meters between updates minimum distance between updates

    @Provides

    fun locationProvider(context: Context, locationRequest: LocationRequest): LocationProvider {
        return LocationProviderImpl(context, locationRequest)
    }

    @Provides
    @ApplicationScope
    fun provideUserRepository(accountService: AccountService): UserRepository =
        UserRepository(accountService)


    @Provides
    fun provideMyRoom(context: Context): MyRoom = MyRoomImpl(context)

    @Provides
    fun provideGameRepository(
        settingsRepository: SettingsRepository,
        userRepository: UserRepository,
        roomMaintainer: MyRoom,
        context: Context
    ): GameRepositoryImpl =
        GameRepositoryImpl(
            settingsRepository,
            userRepository,
            roomMaintainer,
            context
        )

    @Provides
    @ApplicationScope
    fun providePermissionsService(context: Context): PermissionsService =
        PermissionsService(context)

    @Provides
    @ApplicationScope
    fun provideLocationSetting(
        context: Context,
        locationRequest: LocationRequest
    ): LocationSettingsService =
        LocationSettingsService(context, locationRequest)


    @Provides
    @ApplicationScope
    fun provideSettingRepository(
        locationSettings: LocationSettingsService, permissionsService: PermissionsService,
        settingService: SettingService
    ): SettingsRepository =
        SettingsRepository(locationSettings, permissionsService, settingService)

}


@Module
class ActivityModule(private val activity: AppCompatActivity) {

    //@Provides annotation tells Dagger where to find it dataType
    //@Singleton, only one instance of this object

    @Provides
    @ActivityScope
    fun provideActivity() = activity
//
//    @Provides
//    @ActivityContextQualifier
//    @ActivityScope
//    fun provideContext() = activity.baseContext

}


@Module
class FragmentModule(private val fragment: Fragment) {

    //@Provides annotation tells Dagger where to find it dataType
    //@Singleton, only one instance of this object

//    @Provides
//    @FragmentScope
//    fun provide() = fragment

}
