package com.example.myLifeLog

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import com.example.myLifeLog.model.apimanager.ActivityTransitionManager
import com.example.myLifeLog.model.apimanager.SleepManager
import com.example.myLifeLog.model.db.timelog.TimeLog
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.zip.CRC32

fun getAppName(packageName: String, context: Context): String {
    return try {
        packageManager(context).getApplicationLabel(packageName).toString()
    } catch(e:Exception){
        packageName
    }
}

//StringからCRC32によってハッシュ値をつくり、その値からColorを生成する。non_transparency: 0 ~ 255
fun String.toColor(non_transparency: Int = 255): Color {
    if ((non_transparency in 0..255).not()) {
        error("non_transparency must be in the range 0 to 255")
    }
    val crc = CRC32()
    crc.update(this.hashCode())
    val hash:String
        = Integer.toHexString(non_transparency) + Integer.toHexString(crc.value.toInt()).substring(0..5).uppercase()
    return Color(hash.toUInt(16).toInt())
}

fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun LocalDateTime.toMilliSec(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

//ミリ秒単位のLongを、”x時間y分z.w秒"に変換して返す。
fun Long.toHMS(): String {
    val hours: Long = this / 1000 / 60 / 60
    val minutes: Long = this / 1000 / 60 % 60
    val seconds: Long = this / 1000 % 60
    val bellowSec: Long = this % 1000 / 100 //秒より下は1桁まで
    return "${hours}時間 ${minutes}分 ${seconds}.${bellowSec}秒"
}

fun loadSetting(context: Context, key: String): Boolean {
    val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    return sharedPref.getBoolean(key, false)
}

fun setSetting(context: Context, key: String, bool: Boolean) {
    val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putBoolean(key, bool).apply()
}

fun setLastUpdateTime(context: Context, contentType: String, dateTime: LocalDateTime) {
    val sharedPref = context.getSharedPreferences("LastUpdateTime", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putLong(contentType, dateTime.toMilliSec()).apply()
}

fun loadLastUpdateTime(context: Context, contentType: String): LocalDateTime {
    val sharedPref = context.getSharedPreferences("LastUpdateTime", Context.MODE_PRIVATE)
    return sharedPref.getLong(contentType, 0L).toLocalDateTime()
}

fun setLastLocation(context: Context, content: String) {
    val sharedPref = context.getSharedPreferences("LastLocation", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putString("location", content).apply()
}

fun loadLastLocation(context: Context): String? {
    val sharedPref = context.getSharedPreferences("LastLocation", Context.MODE_PRIVATE)
    return sharedPref.getString("location", "")
}
fun setLastSleepState(context: Context, content: String) {
    val sharedPref = context.getSharedPreferences("LastSleepState", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putString("sleep", content).apply()
}

fun loadLastSleepState(context: Context): String? {
    val sharedPref = context.getSharedPreferences("LastSleepState", Context.MODE_PRIVATE)
    return sharedPref.getString("sleep", "")
}

data class LocContent(
    val activityType: Int,
    val locId: Int?,
    val lat: Double?,
    val lon: Double?
)

fun String.toLocContent(): LocContent{
    return this.split(",").let{
        LocContent(it[0].toInt(), it[1].toIntOrNull(), it[2].toDoubleOrNull(), it[3].toDoubleOrNull())
    }
}

// For test. 各項目に10万件ログを入れてどうなるか
private fun insertBigRecords(viewModel: MainViewModel) {
    val tabData = listOf("場所","睡眠", "アプリ")
    tabData.forEach { tabText ->
        repeat(100000) {
            viewModel.insertTimeLog(
                TimeLog(
                    contentType = tabText,
                    timeContent = "",
                    fromDateTime = LocalDateTime.of(2022,1,1,1,1),
                    untilDateTime = LocalDateTime.of(2022,1,1,2,1)
                )
            )
        }
    }
}

fun subscribeAT(context: Context){
    val contentType = "location"
    val key = "IsActivityRecognitionSubscribed"
    val activityTransitionManager = ActivityTransitionManager.getInstance(context)
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    if (!loadSetting(context, key=key)) { /*すでにサブスクライブされてるかチェック*/
//     ・ActivityRecognitionTransitionAPIにサブスクライブ
//     ・Settings：IsActivityRecognitionSubscribedをtrueに
//     ・lastLocation・lastUpdateTimeを更新
        Log.d("", "subscribe to ActivityRecognition")
        activityTransitionManager.startActivityUpdate()
        setSetting(context, key=key, bool=true)
        setLastUpdateTime(context, contentType, LocalDateTime.now())
        doSomethingWithLocation(
            context, locationClient,
            onSuccess = {location -> setLastLocation(
                context, "3,,${location.latitude},${location.longitude}"
            )},
            onFailure = {setLastLocation(
                context, "3,,null,null"
            )}
        )
    }
}

fun stopSubscribeAT(context: Context, updateLocationLogs: () -> Unit){
    val key = "IsActivityRecognitionSubscribed"
    val activityTransitionManager = ActivityTransitionManager.getInstance(context)
    if (loadSetting(context, key=key)) {
//     ・ActivityRecognitionTransitionAPIのサブスクライブを解除
//     ・Settings：IsActivityRecognitionSubscribedをfalseに
//     ・updateLocationLogを実行
        activityTransitionManager.stopActivityUpdate()
        setSetting(context, key=key, bool=false)
        updateLocationLogs()
    }
}

fun subscribeSleep(context: Context){
    val contentType = "sleep"
    val key = "IsSleepDetectionSubscribed"
    val sleepManager = SleepManager.getInstance(context)
    if (!loadSetting(context, key=key)) { /*すでにサブスクライブされてるかチェック*/
        Log.d("subscribeSleep", "subscribe to ActivityRecognition")
//     ・SleepAPIにサブスクライブ
//     ・Settings：IsSleepDetectionSubscribedをtrueに
//     ・lastSleep・lastUpdateTimeを更新
        sleepManager.startSleepUpdate()
        setSetting(context, key=key, bool=true)
        setLastUpdateTime(context, contentType, LocalDateTime.now())
        setLastSleepState(context, "awake")
    }
}

fun stopSubscribeSleep(context: Context){
    val key = "IsSleepDetectionSubscribed"
    val sleepManager = SleepManager.getInstance(context)
    if (loadSetting(context, key=key)) { /*すでにサブスクライブされてるかチェック*/
        sleepManager.stopSleepUpdate()
        setSetting(context, key=key, bool=false)
    }
}

@SuppressLint("MissingPermission")
fun doSomethingWithLocation(context: Context,
                            locationClient: FusedLocationProviderClient,
                            onSuccess: (android.location.Location) -> Unit,
                            onFailure: () -> Unit
){
    val tag = "doSomethingWithLocation"
    // check permission
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        == PackageManager.PERMISSION_GRANTED
    ) {
        Log.d(tag, "try to get location")
        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }
        ).addOnSuccessListener { location ->
            Log.d(tag, "get location")
            if (location != null) {
                Log.d(tag, "location: ${location.latitude} ${location.longitude}")
                onSuccess(location)
            } else {
                Log.d(tag, "null location")
                onFailure()
            }
        }.addOnFailureListener {
            Log.d(tag, "can not get location")
            onFailure()
        }
    } else {
        Log.d(tag, "permission denied (either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION is required)")
        return
    }
}