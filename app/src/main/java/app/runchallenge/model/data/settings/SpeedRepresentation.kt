package app.runchallenge.model.data.settings

//derived from setting_values.xml
enum class SpeedRepresentation(val value: Int) {
    kilometer_per_hour(1),
    meter_per_sec(2),
    min_per_kilometer(3),
    mile_per_hour(4),
    feet_per_sec(5),
    min_per_mile(6)
}