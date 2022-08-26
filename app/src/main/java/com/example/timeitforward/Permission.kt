package com.example.timeitforward

import android.Manifest
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.vmadalin.easypermissions.EasyPermissions

// TODO
class Permission(private val activity: MainActivity) {
    private val requestCode = 0
    private val isAPILevel29orAbove29 = Build.VERSION.SDK_INT >= 29

    fun requestActivityRecognitionPermission() {
        val permissions = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
        val rationale = activity.getString(R.string.request_activity_recognition_permission_rational)
        if (isAPILevel29orAbove29) {
            requestPermission(rationale, requestCode, permissions)
        }
    }

    fun requestBackgroundLocationPermission() {
        if (isAPILevel29orAbove29) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            val rationale = activity.getString(R.string.request_background_location_permission_rational_API29orAbove29)
            requestPermission(rationale, requestCode, permissions)
        } else {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
            val rationale = activity.getString(R.string.request_background_location_permission_rational_APIUnder29)
            requestPermission(rationale, requestCode, permissions)
        }
    }

    private fun requestPermission(rationale: String, requestCode: Int, permissions: Array<String>) {
        if (!EasyPermissions.hasPermissions(activity, *permissions)) {
            Log.v("Permission", "Have no permission: ${permissions.joinToString()}")
            EasyPermissions.requestPermissions(activity, rationale, requestCode, *permissions)
        }
    }

    fun requestUsageStatsPermission() {
        val permissions = arrayOf(Manifest.permission.PACKAGE_USAGE_STATS)
        if (!checkUsageStatsPermission()) {
            Log.v("Permission", "Have no permission: ${permissions.joinToString()}")
            AlertDialog.Builder(activity)
                .setTitle("使用状況へのアクセス")
                .setMessage(activity.getString(R.string.request_usage_stats_permission_rational))
                .setPositiveButton(
                    "設定"
                ) { _, _ ->
                    ContextCompat.startActivity(
                        activity,
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        null
                    )
                }
                .setNegativeButton(
                    "キャンセル"
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .setCancelable(true)
                .show()
        }
    }

    // True if the permission of usageStatsManager is allowed, else False
    private fun checkUsageStatsPermission(): Boolean {
        val aom: AppOpsManager = activity.getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            aom.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(), activity.packageName
            )
        } else {
            aom.checkOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(), activity.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
