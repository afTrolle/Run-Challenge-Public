package app.runchallenge.model.data.settings

import androidx.appcompat.app.AppCompatDelegate


data class ApplicationSettings(
    val measurementType: UnitMeasurement = UnitMeasurement.Metric,
    val speedPacingRepresentation: SpeedRepresentation = SpeedRepresentation.min_per_kilometer,
    val isAudioOn: Boolean = true,
    val volume: Int = 100,
    val isPlacementFeedback: Boolean = true,
    val distanceBetweenUpdates: Int = 0,
    val themeMode: Int = AppCompatDelegate.MODE_NIGHT_NO
)

enum class UnitMeasurement {
    Imperial, Metric
}
