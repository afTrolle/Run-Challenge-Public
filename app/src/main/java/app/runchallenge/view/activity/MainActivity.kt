package app.runchallenge.view.activity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManagerFix
import app.runchallenge.R
import app.runchallenge.dagger.ActivityComponent
import app.runchallenge.dagger.ActivityModule
import app.runchallenge.dagger.DaggerActivityComponent
import app.runchallenge.dagger.DaggerViewModelFactory
import app.runchallenge.model.extensions.setTextColor
import app.runchallenge.controller.repository.SettingsRepository
import app.runchallenge.view.application.MyApplication
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


val Activity.sharedPreferences: SharedPreferences get() = PreferenceManagerFix.getDefaultSharedPreferences(this)


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: DaggerViewModelFactory

    @Inject
    lateinit var activityViewModel: ActivityViewModel

    lateinit var activityComponent: ActivityComponent

    @Inject
    lateinit var settingsRepository: SettingsRepository


    private fun daggerInit(mApplication: MyApplication, activity: AppCompatActivity): ActivityComponent =
        DaggerActivityComponent.builder()
            .activityModule(ActivityModule(activity))
            .applicationComponent(mApplication.applicationComponent)
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init dagger
        activityComponent = daggerInit(application as MyApplication, this)
        activityComponent.inject(this)
        //get view model
        activityViewModel = ViewModelProviders.of(this, viewModelFactory).get(ActivityViewModel::class.java)

        sharedPreferences.registerOnSharedPreferenceChangeListener(activityViewModel)

        activityViewModel.myActivitySnackBar.observe(this, mySnackObserver)

        setContentView(R.layout.activity_main)
        trackThemeMode()
    }

    private fun trackThemeMode() {
        settingsRepository.observer(this, {
            val desiredMode = it.appSettings.themeMode
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            if (desiredMode != currentMode) {
                AppCompatDelegate.setDefaultNightMode(desiredMode)
                recreate()
            }
        }, {})
    }


    private var snackbar: Snackbar? = null

    private val mySnackObserver = Observer<Int?> { i ->
        if (i == null) {
            //remove active sna
            snackbar?.dismiss()
        } else {
            snackbar = Snackbar.make(
                myCoordinatorLayout, i,
                Snackbar.LENGTH_SHORT
            ).also { snackbar ->
                snackbar.setAction(R.string.exit) {
                    this.onBackPressed()
                    snackbar.dismiss()
                }
                snackbar.setActionTextColor(getColor(R.color.onAccent))
                snackbar.setTextColor(getColor(R.color.onAccent))
            }
            snackbar?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(activityViewModel)
    }


    override fun onSupportNavigateUp(): Boolean {
        if (activityViewModel.onBackPressed()) {
            NavHostFragment.findNavController(nav_host_fragment).navigateUp()
            return true
        }
        return false
    }

    override fun onNavigateUp(): Boolean {
        if (activityViewModel.onBackPressed()) {
            return super.onNavigateUp()
        } else {
            return false
        }
    }

    override fun onBackPressed() {
        if (activityViewModel.onBackPressed())
            super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        activityViewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityViewModel.onActivityResult(requestCode, resultCode, data)
    }


}
