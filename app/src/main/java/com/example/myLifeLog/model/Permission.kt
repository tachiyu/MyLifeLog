package com.example.myLifeLog.model

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, "com.google.android.gms.permission.ACTIVITY_RECOGNITION") == PackageManager.PERMISSION_GRANTED
    }
}

// True if the permission of usageStatsManager is allowed, else False
fun checkUsageStatsPermission(context: Context): Boolean {
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
        val permissions = if (Build.VERSION.SDK_INT >= 29) {
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
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

fun requestActivityAndLocationPermission(context: Context) {
    if (checkActivityPermission(context) && checkLocationPermission(context)) {
        return
    }

    val permissions = mutableListOf<String>()

    if (!checkLocationPermission(context)) {
        if (Build.VERSION.SDK_INT >= 29) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (!checkActivityPermission(context)) {
        val activityRecognitionPermission = if (Build.VERSION.SDK_INT >= 29) {
            Manifest.permission.ACTIVITY_RECOGNITION
        } else {
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
        }
        permissions.add(activityRecognitionPermission)
    }

    ActivityCompat.requestPermissions(
        context as Activity,
        permissions.toTypedArray(),
        0
    )
}
