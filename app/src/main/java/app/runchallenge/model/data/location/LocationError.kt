package app.runchallenge.model.data.location

import java.lang.Exception

data class LocationError(
    override val message: String,
    val resolution: Exception? = null
) : Throwable()