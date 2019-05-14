package app.runchallenge.controller.provider

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.runchallenge.R
import app.runchallenge.model.extensions.livedata.MyLiveData
import app.runchallenge.model.data.settings.Permissions
import app.runchallenge.model.extensions.log

private const val LOCATION_REQUEST_CODE = 1000


class PermissionsService constructor(private val context: Context) : MyLiveData<Permissions, String>() {


    //category of permissions

    private val gpsPermissions = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)

    //all permissions
    private val usedPermissions = gpsPermissions

    private var requestingPermissions: Boolean = false
    private var permissionsBeingRequests: Array<String> = usedPermissions

    //current value permissions
    // private val permission: Map<String, Boolean> get() = getPermissions()

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED


    //private fun getPermissions() = usedPermissions.map { s: String -> Pair(s, hasPermission(s)) }.toMap()

    private fun hasUsedPermissions(): Boolean {
        //contains false, ie missing gps permission
        return !usedPermissions.map { s: String -> hasPermission(s) }.contains(false)
    }

    private fun hasGpsPermissions(): Boolean {
        //contains false, ie missing gps permission
        return !gpsPermissions.map { s: String -> hasPermission(s) }.contains(false)
    }


    fun requestGPSPermission(activity: Activity) {
        if (!requestingPermissions) {
            setWorking()
            requestingPermissions = true
            permissionsBeingRequests = gpsPermissions
            ActivityCompat.requestPermissions(
                activity, gpsPermissions,
                LOCATION_REQUEST_CODE
            )
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                requestingPermissions = false
                if (permissions.isNotEmpty() && grantResults.filter { it == PackageManager.PERMISSION_GRANTED }.size == grantResults.size) {
                    //user took action
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    onPermissions(permissionsBeingRequests)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //user cancel permissions request
                    onError(permissionsBeingRequests)
                }
            }
        }
    }


    /**
     * called when we do have permissions
     */
    private fun onPermissions(permissions: Array<out String>) {
        if (permissions.contentEquals(gpsPermissions))
            setValue {
                it?.copy(gps = true) ?: Permissions(gps = true)
            }
    }

    /**
     * called when we don't have permissions
     */
    private fun onNotPermissions(permissions: Array<String>) {
        var gpsNotContained = false

        gpsPermissions.forEach {
            if (!permissions.contains(it)) {
                gpsNotContained = true
            }
        }

        setValue {
            var newValue = it ?: Permissions()

            if (gpsNotContained)
                newValue.copy(gps = false)
            //add more permissions
            return@setValue newValue
        }
    }

    /**
     * failed to retrieve permissions
     */
    private fun onError(permissionsBeingRequests: Array<String>) {
        val errorMessage = context.getString(R.string.missing_gps_permission)
        setError(errorMessage)
    }

    override fun onInactive() {
        super.onInactive()
        silentGetPermissions()
    }

    override fun onActive() {
        log("onActive")
        silentGetPermissions()
    }

    private fun silentGetPermissions() {
        if (hasUsedPermissions()) {
            onPermissions(usedPermissions)
        } else {
            onNotPermissions(usedPermissions)
        }
    }

}

