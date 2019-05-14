package app.runchallenge.controller.provider.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.os.HandlerThread
import app.runchallenge.R
import app.runchallenge.model.data.MissingPermission
import app.runchallenge.model.data.location.LocationData
import app.runchallenge.model.data.location.LocationError
import app.runchallenge.model.data.location.LocationState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import java.lang.Exception
import app.runchallenge.model.extensions.*
import app.runchallenge.model.extensions.event.EventObservable

/**
 * listen to location updates
 *
 * Flow
 *      IDLE -> Active -> Finished
 * or
 *      IDLE -> Error
 *
 * */
class LocationProviderImpl(
    private val context: Context,
    private val locationRequest: LocationRequest
) :
    LocationProvider,
    LocationCallback() {

    //we might have back pressure, ie get to many location updates to handle that this thread runs att 100% and can't keep up.

    //one thread for handling locations (Ie Sort locations)!
    private val providerHandlerThread = HandlerThread("LocationProviderThread")

    //fused location library
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    //OBS!! Only one thread is changing state!!!!!!!
    private var state = LocationData()

    //observable
    override
    val locationObservable = EventObservable<LocationState, LocationError>(LocationState.Idle(state))

    override fun onLocationResult(locationResult: LocationResult?) {
        locationResult ?: return //ignore if null
        val newState = state.onLocationResult(locationResult)
        locationObservable.onValue(LocationState.Active(newState))
        state = newState
    }


    override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
        locationAvailability ?: return //ignore if null
        val newState = state.onLocationAvailability(locationAvailability)
        locationObservable.onValue(LocationState.Active(newState))
        state = newState
    }

/* public functions */

    @SuppressLint("MissingPermission")
    override fun startLocationProvider() {
        if (context.hasPermission(ACCESS_FINE_LOCATION)) {
            providerHandlerThread.start()
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                this,
                providerHandlerThread.looper
            ).addOnFailureListener {
                onStartError(it)
            }
        } else {
            onStartError(MissingPermission(ACCESS_FINE_LOCATION))
        }
    }

    override fun stopLocationProvider() {
        fusedLocationProviderClient.removeLocationUpdates(this).addOnCompleteListener {
            locationObservable.onValue(LocationState.Terminated(state))
            providerHandlerThread.quit()
        }
    }

    private fun onStartError(it: Exception) {
        val errorMessage = it.localizedMessage ?: context.getString(R.string.error_start_gps)
        val error = if (it is ResolvableApiException) {
            LocationError(errorMessage, it)
        } else {
            LocationError(errorMessage, null)
        }
        locationObservable.onError(error)
    }

}




