package app.runchallenge.controller.repository

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import app.runchallenge.model.extensions.livedata.MyLiveData
import app.runchallenge.controller.provider.LocationSettingsService
import app.runchallenge.controller.provider.PermissionsService
import app.runchallenge.controller.provider.SettingService
import app.runchallenge.model.extensions.log
import app.runchallenge.model.data.settings.ApplicationSettings
import app.runchallenge.model.data.settings.LocationSetting
import app.runchallenge.model.data.settings.Permissions
import app.runchallenge.model.data.settings.Setting

class SettingsRepository constructor(
    private val locationSettingsService: LocationSettingsService,
    private val permissionsService: PermissionsService,
    private val settingsService: SettingService
) : MyLiveData<Setting, String>() {


    init {
        addMergeSource(locationSettingsService, permissionsService, settingsService,
            { locationSetting: LocationSetting, permissions: Permissions, appSettings: ApplicationSettings ->
                log("onChange called")
                setValue {
                    Setting(
                        hasPermissions = permissions.gps,
                        hasPhoneSetting = locationSetting.correctSettings,
                        appSettings = appSettings
                    )
                }
            },
            { locationError: String?, permissionsError: String?, _: String? ->
                when {
                    locationError != null -> setError(locationError)
                    permissionsError != null -> setError(permissionsError)
                }
            })

    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        locationSettingsService.onActivityResult(requestCode, resultCode, data)
    }

    fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        settingsService.onSharedPreferenceChanged(sharedPreferences, key)
    }

    fun getSettings(activity: Activity) {
        //don't do more work if already working
        if (permissionsService.isWorking || locationSettingsService.isWorking) {
            return
        }

        val permissions = permissionsService.success
        val locationSettings = locationSettingsService.success

        if (permissions != null && !permissions.gps) {
            permissionsService.requestGPSPermission(activity)
        } else if (locationSettings != null && !locationSettings.correctSettings) {
            locationSettingsService.checkLocationSettings(activity)
        }
    }


}