package app.runchallenge.view.fragment.preferences

import android.content.Context
import android.os.Bundle
import com.takisoft.preferencex.PreferenceFragmentCompat
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.SwitchPreference
import app.runchallenge.dagger.DaggerFragmentComponent
import app.runchallenge.dagger.FragmentModule
import app.runchallenge.model.data.settings.ApplicationSettings
import app.runchallenge.model.data.settings.Setting
import app.runchallenge.model.data.settings.UnitMeasurement
import app.runchallenge.controller.repository.SettingsRepository
import app.runchallenge.controller.repository.UserRepository
import app.runchallenge.model.data.toolbar.MyToolbarSettings
import app.runchallenge.view.fragment.base.mainActivity
import app.runchallenge.view.fragment.base.updateToolbar
import com.pavelsikun.seekbarpreference.SeekBarPreferenceCompat
import javax.inject.Inject
import android.util.TypedValue
import app.runchallenge.R


class PreferenceFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var userRepository: UserRepository

    private val myToolbarSettings = MyToolbarSettings(
        title = R.string.toolbar_menu_settings_text,
        showBackButton = true
    )

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        mainActivity?.let {
            val fragmentComponent = DaggerFragmentComponent.builder().fragmentModule(FragmentModule(this))
                .activityComponent(it.activityComponent).build()
            fragmentComponent.inject(this)
        }

        findPreference("pref_eula").setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_PreferenceFragment_to_EULAFragment)
            true // True if the click was handled.
        }

        findPreference("pref_about").setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_PreferenceFragment_to_aboutFragment)
            true //  True if the click was handled.
        }
        updateToolbar(myToolbarSettings)
    }


    fun dpToPx(dps: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dps,
            resources.displayMetrics
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        settingsRepository.observer(this,
            { response: Setting ->
                setUi(response.appSettings)
                //     setTheme(response.appSettings.themeMode)
            }, { error ->

            })





        findPreference("pref_sign_out").setOnPreferenceClickListener {
            userRepository.signOut()
            findNavController().popBackStack()
            return@setOnPreferenceClickListener true
        }

        findPreference("pref_auto_manage_theme").setOnPreferenceChangeListener { preference, newValue ->


            return@setOnPreferenceChangeListener true
        }

        findPreference("pref_always_dark_theme").setOnPreferenceChangeListener { preference, newValue ->


            return@setOnPreferenceChangeListener true
        }

        val autoTheme = findPreference("pref_auto_manage_theme") as SwitchPreference
        val darkThemeAlways = findPreference("pref_always_dark_theme") as SwitchPreference
        darkThemeAlways.isEnabled = !autoTheme.isChecked

    }

//    fun setTheme(currentMode: Int) {
//        val mActivity = activity?.let { it as MainActivity }
//        // AppCompatDelegate.setDefaultNightMode(themeMode)
//        val defaultMode = AppCompatDelegate.getDefaultNightMode()
//        if (currentMode != defaultMode) {
//            AppCompatDelegate.setDefaultNightMode(currentMode)
//            mActivity?.recreate()
//        }
//        //   mActivity?.delegate?.setLocalNightMode(themeMode)
//    }


    override fun onResume() {
        super.onResume()
        updateToolbar(myToolbarSettings)
    }


    private fun setUi(applicationSettings: ApplicationSettings) {
        val findPreference: ListPreference = findPreference("pref_speed_or_pace") as ListPreference
        val thisContext: Context? = context

        if (thisContext != null && applicationSettings.measurementType == UnitMeasurement.Imperial) {
            //imperial
            setDistanceFeedBackMeasureUnit(R.string.pref_distance_beep_measure_unit_imperial)

            findPreference.setEntryValues(R.array.preferences_speed_imperial_values)
            findPreference.setEntries(R.array.preferences_speed_imperial)

        } else {
            //metric
            setDistanceFeedBackMeasureUnit(R.string.pref_distance_beep_measure_unit_metric)

            findPreference.setEntryValues(R.array.preferences_speed_metric_values)

            findPreference.setEntries(R.array.preferences_speed_metric)
        }

        val index = findPreference.entryValues.indexOf(applicationSettings.speedPacingRepresentation.value.toString())
        if (index == -1) {
            findPreference.setValueIndex(0)
        } else {
            findPreference.setValueIndex(index)
        }


        //enable disable audio controller
        val volumeSlider: SeekBarPreferenceCompat = findPreference("pref_audio_volume") as SeekBarPreferenceCompat
        volumeSlider.isEnabled = applicationSettings.isAudioOn

        val distanceFeedbackSeekBar = findPreference("pref_distance_feedback") as SeekBarPreferenceCompat
        distanceFeedbackSeekBar.isEnabled = applicationSettings.isAudioOn

        if (distanceFeedbackSeekBar.currentValue == 0) {
            distanceFeedbackSeekBar.setSummary(R.string.pref_distance_feedback_summary_disabled)
        } else {
            distanceFeedbackSeekBar.summary =
                getString(R.string.pref_distance_feedback_summary) + " " + distanceFeedbackSeekBar.currentValue + " " + distanceFeedbackSeekBar.measurementUnit
        }

        val autoTheme = findPreference("pref_auto_manage_theme") as SwitchPreference
        val darkThemeAlways = findPreference("pref_always_dark_theme") as SwitchPreference
        darkThemeAlways.isEnabled = !autoTheme.isChecked
    }


    private fun setDistanceFeedBackMeasureUnit(id: Int) {
        val distanceFeedbackSeekBar = findPreference("pref_distance_feedback") as SeekBarPreferenceCompat
        distanceFeedbackSeekBar.measurementUnit = getString(id)
    }

}
