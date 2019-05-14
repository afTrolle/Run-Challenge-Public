package app.runchallenge.controller.provider.location

import app.runchallenge.model.data.location.LocationError
import app.runchallenge.model.data.location.LocationState
import app.runchallenge.model.extensions.event.EventObservable


interface LocationProvider {

    val locationObservable: EventObservable<LocationState, LocationError>

    fun startLocationProvider()

    fun stopLocationProvider()

}