package app.runchallenge.controller.provider

import android.app.Activity
import android.content.Context
import android.content.Intent
import app.runchallenge.R
import app.runchallenge.model.extensions.livedata.MyLiveData
import app.runchallenge.model.data.settings.LocationSetting
import app.runchallenge.model.extensions.log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

const val REQUEST_CHECK_SETTINGS = 1011

class LocationSettingsService constructor(
    private val context: Context,
    locationRequest: LocationRequest
) : MyLiveData<LocationSetting, String>() {

    override var default: LocationSetting? =
        LocationSetting()

    private val settingsClient = LocationServices.getSettingsClient(context)

    private val locationSettingsRequest =
        LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true).build()

    /**
     * check that current gps settings a fulfilled
     * */
    fun checkLocationSettings(activity: Activity) {
        setWorking()
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnFailureListener {
                when (it) {
                    is ResolvableApiException -> {
                        it.startResolutionForResult(activity,
                            REQUEST_CHECK_SETTINGS
                        )
                    }
                    else -> onError(it.localizedMessage)
                }
            }
            .addOnSuccessListener {
                onSuccess(it.locationSettingsStates)
            }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            val states = LocationSettingsStates.fromIntent(data)
            when (resultCode) {
                Activity.RESULT_OK -> onSuccess(states)
                else -> onError(null)
            }
        }
    }

    override fun onInactive() {
        silentGetSettings()
    }

    override fun onActive() {
        log("onActive")
        silentGetSettings()
    }

    private fun silentGetSettings() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnFailureListener {
                onUnknown()
            }
            .addOnSuccessListener {
                onSuccess(it.locationSettingsStates)
            }
    }


    private fun onError(localizedMessage: String?) {
        val message = localizedMessage ?: context.getString(R.string.error_set_location_settings)
        setError(message)
    }

    private fun onUnknown() {
        setDefaultValue {
            it.copy(correctSettings = false)
        }
    }

    private fun onSuccess(states: LocationSettingsStates) {
        setDefaultValue {
            it.copy(correctSettings = true)
        }
    }


}

