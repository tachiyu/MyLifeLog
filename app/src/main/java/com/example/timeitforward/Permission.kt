package com.example.timeitforward

import android.Manifest
import android.os.Build
import android.util.Log
import com.vmadalin.easypermissions.EasyPermissions

// TODO
object Permission {
    val requestCode = 0
    val isAPILevel29orAbove29 = Build.VERSION.SDK_INT >= 29

    fun requestActivityRecognitionPermission(activity: MainActivity) {
        val permissions = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
        val rational = activity.getString(R.string.request_activity_recognition_permission_rational)
        if (isAPILevel29orAbove29) {
            requestPermission(activity, rational, requestCode, permissions)
        }
    }

    fun requestBackgroundLocationPermission(activity: MainActivity) {
        if (isAPILevel29orAbove29) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            val rational = activity.getString(R.string.request_background_location_permission_rational_API29orAbove29)
            requestPermission(activity, rational, requestCode, permissions)
        } else {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
            val rational = activity.getString(R.string.request_background_location_permission_rational_APIUnder29)
            requestPermission(activity, rational, requestCode, permissions)
        }
    }

    private fun requestPermission(activity: MainActivity, rational: String,
                                  requestCode: Int, permissions: Array<String>) {
        if (!EasyPermissions.hasPermissions(activity, *permissions)) {
            Log.v("Permission", "Have no permission: ${permissions.joinToString()}")
            EasyPermissions.requestPermissions(activity, rational, requestCode, *permissions)
        }
    }
}
