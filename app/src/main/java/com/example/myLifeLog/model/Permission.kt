package com.example.myLifeLog.model

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myLifeLog.R

fun checkLocationPermission(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= 29) {
        ((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED))
                && (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                )
    } else {
        (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }


fun checkActivityPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= 29) {
        (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED)
    } else {
        true
    }
}

// True if the permission of usageStatsManager is allowed, else False
private fun checkUsageStatsPermission(context: Context): Boolean {
    val aom: AppOpsManager = context.getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        aom.unsafeCheckOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
    } else {
        aom.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}


fun requestActivityPermission(context: Context) {
    if (!checkActivityPermission(context)) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION"),
            0
        )
    }
}

fun requestLocationPermission(context: Context) {
    if (!checkLocationPermission(context)) {
        if (Build.VERSION.SDK_INT >= 29) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                0
            )
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }
}

fun requestActivityAndLocationPermission(context: Context) {
    if (checkActivityPermission(context) && checkLocationPermission(context)) {
        return
    } else {
        val permissions
             = if (!checkActivityPermission(context) && !checkLocationPermission(context)){
                    if (Build.VERSION.SDK_INT >= 29) {
                        arrayOf(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACTIVITY_RECOGNITION
                        )
                    } else {
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
                        )
                    }
                } else if (!checkLocationPermission(context)) {
                    if (Build.VERSION.SDK_INT >= 29) {
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    } else {
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                } else {
                    arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
                }
        ActivityCompat.requestPermissions(
            context as Activity,
            permissions,
            0
        )
    }
}

fun requestUsageStatsPermission(context: Context) {
    val permissions = arrayOf(Manifest.permission.PACKAGE_USAGE_STATS)
    if (!checkUsageStatsPermission(context)) {
        Log.v("Permission", "Have no permission: ${permissions.joinToString()}")
        AlertDialog.Builder(context)
            .setTitle("使用状況へのアクセス")
            .setMessage(context.getString(R.string.request_usage_stats_permission_rational))
            .setPositiveButton(
                "設定"
            ) { _, _ ->
                ContextCompat.startActivity(
                    context,
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
