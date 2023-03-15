package com.example.myLifeLog

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.myLifeLog.model.apimanager.ActivityTransitionManager
import com.example.myLifeLog.model.apimanager.SleepManager
import com.example.myLifeLog.model.checkLocationPermission
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.io.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
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

//LocalDateを〇年〇月〇日〇曜日にパースする
fun LocalDate.toYMDE(): String {
    return this.format(DateTimeFormatter.ofPattern("yyyy年 MM月 dd日 (E)", Locale.JAPANESE))
}

fun saveSharedPref(context: Context, key: String, bool: Boolean) {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putBoolean(key, bool).apply()
}

fun saveSharedPref(context: Context, key: String, long: Long) {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putLong(key, long).apply()
}

fun saveSharedPref(context: Context, key: String, str: String) {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putString(key, str).apply()
}

fun saveSharedPref(context: Context, key: String, int: Int) {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putInt(key, int).apply()
}

fun loadSharedPrefBool(context: Context, key: String): Boolean {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    return sharedPref.getBoolean(key, false)
}

fun loadSharedPrefLong(context: Context, key: String): Long {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    return sharedPref.getLong(key, 0L)
}

fun loadSharedPrefStr(context: Context, key: String): String? {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    return sharedPref.getString(key, "")
}

fun loadSharedPrefInt(context: Context, key: String): Int {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    return sharedPref.getInt(key, 0)
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

fun subscribeAT(context: Context){
    val key = "IsActivityRecognitionSubscribed"
    val activityTransitionManager = ActivityTransitionManager.getInstance(context)
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    if (!loadSharedPrefBool(context, key=key)) { /*すでにサブスクライブされてるかチェック*/
//     ・ActivityRecognitionTransitionAPIにサブスクライブ
//     ・Settings：IsActivityRecognitionSubscribedをtrueに
//     ・lastLocation・lastUpdateTimeを更新
        myLog("", "subscribe to ActivityRecognition")
        activityTransitionManager.startActivityUpdate()
        saveSharedPref(context, key=key, bool=true)
        saveSharedPref(context, "lastLocationUpdatedTime", LocalDateTime.now().toMilliSec())
        doSomethingWithLocation(
            context, locationClient, 1, 5,
            onSuccess = {location ->
                saveSharedPref(
                    context,
                    "lastLocation",
                    "3,,${location.latitude},${location.longitude}"
                )
            },
            onFailure = {
                saveSharedPref(
                    context,
                    "lastLocation",
                    "3,,null,null"
                )
            }
        )
    }
}

fun stopSubscribeAT(context: Context, updateLocationLogs: () -> Unit){
    val key = "IsActivityRecognitionSubscribed"
    val activityTransitionManager = ActivityTransitionManager.getInstance(context)
    if (loadSharedPrefBool(context, key=key)) {
//     ・ActivityRecognitionTransitionAPIのサブスクライブを解除
//     ・Settings：IsActivityRecognitionSubscribedをfalseに
//     ・updateLocationLogを実行
        activityTransitionManager.stopActivityUpdate()
        saveSharedPref(context, key=key, bool=false)
        updateLocationLogs()
    }
}

fun subscribeSleep(context: Context){
    val isSleepSubscribed = "IsSleepDetectionSubscribed"
    val sleepManager = SleepManager.getInstance(context)
    if (!loadSharedPrefBool(context, key=isSleepSubscribed)) { /*すでにサブスクライブされてるかチェック*/
        myLog("subscribeSleep", "subscribe to ActivityRecognition")
//     ・SleepAPIにサブスクライブ
//     ・Settings：IsSleepDetectionSubscribedをtrueに
//     ・lastSleep・lastUpdateTimeを更新
        sleepManager.startSleepUpdate()
        saveSharedPref(context, key=isSleepSubscribed, bool=true)
        saveSharedPref(context, "lastSleepUpdatedTime", LocalDateTime.now().toMilliSec())
        saveSharedPref(context, "lastSleepState","awake")
    }
}

fun stopSubscribeSleep(context: Context){
    val key = "IsSleepDetectionSubscribed"
    val sleepManager = SleepManager.getInstance(context)
    if (loadSharedPrefBool(context, key=key)) { /*すでにサブスクライブされてるかチェック*/
        sleepManager.stopSleepUpdate()
        saveSharedPref(context, key=key, bool=false)
    }
}

fun logToLocal(tag: String, msg: String, time: LocalDateTime) {
    val str = "${time.toString()}/ $tag/ $msg"
    val d = File("/data/data/com.example.myLifeLog/files")
    if (!d.exists()) { d.mkdirs() }
    val f = File("/data/data/com.example.myLifeLog/files/log.txt")
    val bw = BufferedWriter(FileWriter(f, true))
    bw.append(str)
    bw.newLine()
    bw.close()
}

fun myLog(tag: String, msg: String) {
    Log.d(tag, msg)
    logToLocal(tag, msg, LocalDateTime.now())
}

fun readLog(): List<String> {
    val br = BufferedReader(FileReader(File("/data/data/com.example.myLifeLog/files/log.txt")))
    val lines =  br.readLines()
    br.close()
    return lines
}

@SuppressLint("MissingPermission")
fun doSomethingWithLocation(context: Context,
                            locationClient: FusedLocationProviderClient,
                            cnt: Int, //この関数の試行回数
                            maxCnt: Int, //cntがこの値を超えると、試行をあきらめる
                            onSuccess: (android.location.Location) -> Unit,
                            onFailure: () -> Unit ){
    val tag = "doSomethingWithLocation"
    // check permission
    if (checkLocationPermission(context)) {
        myLog(tag, "try to get location (${cnt}th time")
        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }
        ).addOnSuccessListener { location ->
            if (location != null) {
                myLog(tag, "get location: ${location.latitude} ${location.longitude}")
                onSuccess(location)
            } else {
                myLog(tag, "get null location")
                // cnt が maxCntを超えていなければ、再帰的に再試行。
                if (cnt < maxCnt) {
                    doSomethingWithLocation(
                        context, locationClient, cnt+1, maxCnt, onSuccess, onFailure
                    )
                } else {
                    onFailure()
                }
            }
        }.addOnFailureListener {
            myLog(tag, "can not get location")
            // cnt が maxCntを超えていなければ、再帰的に再試行。
            if (cnt < maxCnt) {
                doSomethingWithLocation(
                    context, locationClient, cnt+1, maxCnt, onSuccess, onFailure
                )
            } else {
                onFailure()
            }
        }
    } else {
        myLog(tag, "permission denied (either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION is required)")
        return
    }
}

fun makeFirstLocationLog(
    context: Context,
    locationClient: FusedLocationProviderClient,
) {

}