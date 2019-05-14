package app.runchallenge.view.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.runchallenge.R
import app.runchallenge.controller.repository.SettingsRepository
import javax.inject.Inject

private const val MAX_MILLIS_BETWEEN_BACK_BUTTON: Long = 1500

class ActivityViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

    val myActivitySnackBar: MutableLiveData<Int?> = MutableLiveData()

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        settingsRepository.onSharedPreferenceChanged(sharedPreferences, key)
    }


    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        settingsRepository.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        settingsRepository.onActivityResult(requestCode, resultCode, data)
    }


    /*
     *  Back button handling
     */

    /**
     * called when back button is clicked with elapsed millis
     */

    private var timestamp: Long = 0
    private var doubleClickBackButton: Boolean = false

    fun setDoubleBackButton(state: Boolean) {
        doubleClickBackButton = state
    }

    fun onBackPressed(): Boolean {
        return if (doubleClickBackButton && MAX_MILLIS_BETWEEN_BACK_BUTTON < (SystemClock.elapsedRealtime() - timestamp)) {
            //enableChannel double click needed
            Log.d("derp", "block back button")
            timestamp = SystemClock.elapsedRealtime()
            myActivitySnackBar.postValue(R.string.double_click_alert_message)
            false
        } else {
            myActivitySnackBar.postValue(null)
            true
        }
    }

}