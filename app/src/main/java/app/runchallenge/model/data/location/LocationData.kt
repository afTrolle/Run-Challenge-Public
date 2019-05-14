package app.runchallenge.model.data.location

import android.location.Location
import app.runchallenge.model.extensions.sortLocations
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import java.util.*

sealed class LocationState(open val locationData: LocationData) {
    data class Idle(override val locationData: LocationData) : LocationState(locationData)
    data class Active(override val locationData: LocationData) : LocationState(locationData)
    data class Terminated(override val locationData: LocationData) : LocationState(locationData)
}

data class LocationData(
    val sortedLocations: Set<Location> = setOf(),
    val isLocationAvailable: Boolean = false
) {

    fun onLocationResult(locationResult: LocationResult): LocationData {
        //set of location data
        val newLocations = sortedLocations.plus(locationResult.locations).plusElement(locationResult.lastLocation)

        //this might be stupid if we have a long running app,
        val sorted = newLocations.sortLocations()

        return copy(sortedLocations = sorted)
    }

    fun onLocationAvailability(locationAvailability: LocationAvailability): LocationData {
        return copy(isLocationAvailable = locationAvailability.isLocationAvailable)
    }

}




