package app.runchallenge.controller.provider

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManagerFix
import app.runchallenge.model.extensions.livedata.MyLiveData
import app.runchallenge.model.data.settings.ApplicationSettings
import app.runchallenge.model.data.settings.SpeedRepresentation
import app.runchallenge.model.data.settings.UnitMeasurement

class SettingService constructor(val context: Context) : MyLiveData<ApplicationSettings, String>() {

    private val Context.preferenceManager: SharedPreferences
        get() = PreferenceManagerFix.getDefaultSharedPreferences(
            this
        )

    private val measurementType: UnitMeasurement
        get() = if (context.preferenceManager.getString("pref_measurement_system", "1") == "1") {
            UnitMeasurement.Metric
        } else {
            UnitMeasurement.Imperial
        }


    private val speedPacingRepresentation: SpeedRepresentation
        get() {
            val number: Int =
                context.preferenceManager.getString("pref_speed_or_pace", "1")?.toIntOrNull() ?: 1
            return SpeedRepresentation.values()[number.minus(1)]
        }


    private val themeMode: Int
        get () {
            val autoTheme = context.preferenceManager.getBoolean("pref_auto_manage_theme", false)
            val alwaysDark = context.preferenceManager.getBoolean("pref_always_dark_theme", false)
            return figureThemeMode(autoTheme, alwaysDark)
        }

    fun figureThemeMode(autoTheme: Boolean, alwaysDark: Boolean): Int {
        return when {
            autoTheme -> AppCompatDelegate.MODE_NIGHT_AUTO
            alwaysDark -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
    }


    /**
     * is audio enabled
     */
    private val isAudioOn: Boolean get() = context.preferenceManager.getBoolean("pref_audio_enabled", true)

    /**
     * get volume 0 - 100 %
     */
    private val volume: Int get() = context.preferenceManager.getInt("pref_audio_volume", 100)

    /**
     * if user wants placement notifications
     */
    private val isPlacementFeedback: Boolean get() = context.preferenceManager.getBoolean("pref_placement_audio", true)

    /**
     * give user distance update every x units
     */
    private val distanceBetweenUpdates: Int = context.preferenceManager.getInt("pref_distance_feedback", 500)


    fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "pref_measurement_system") {
            sharedPreferences?.let {
                if (measurementType == UnitMeasurement.Imperial) {
                    it.edit()?.putString("pref_speed_or_pace", "4")?.apply()
                } else {
                    it.edit()?.putString("pref_speed_or_pace", "1")?.apply()
                }
            }
        }
        alertObservers()
    }

    override fun onActive() {
        alertObservers()
    }

    private fun alertObservers() {
        setValue {
            ApplicationSettings(
                measurementType,
                speedPacingRepresentation,
                isAudioOn,
                volume,
                isPlacementFeedback,
                distanceBetweenUpdates,
                themeMode
            )
        }
    }
}

