package com.example.timeitforward

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.timeitforward.model.db.timelog.TimeLog
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.zip.CRC32

fun getAppName(packageName: String, context: Context): String {
    return try {
        packageManager(context).getApplicationLabel(packageName).toString()
    } catch(e:Exception){
        "$packageName"
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

// searchResultsをcontentTypeとdateで絞ってupdateする。
fun updateSearchResultsOfContentTypeBetweenDates(
    fromDate: LocalDate,
    untilDate: LocalDate,
    contentType: String,
    viewModel: MainViewModel
) {
    viewModel.findTimeLogOfContentTypeBetweenDateTimes(
        fromDate.atStartOfDay(),
        untilDate.plusDays(1).atStartOfDay().minusNanos(1),
        contentType
    )
}

fun loadSetting(context: Context, key: String): Boolean {
    val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    return sharedPref.getBoolean(key, false)
}

fun setSetting(context: Context, key: String, value: Boolean) {
    val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putBoolean(key, value).apply()
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

fun setLastLocation(context: Context, contentType: String, content: String) {
    val sharedPref = context.getSharedPreferences("LastLocation", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putString(contentType, content).apply()
}

fun loadLastLocation(context: Context, contentType: String): String? {
    val sharedPref = context.getSharedPreferences("LastLocation", Context.MODE_PRIVATE)
    return sharedPref.getString(contentType, "")
}
fun setLastSleepState(context: Context, contentType: String, content: String) {
    val sharedPref = context.getSharedPreferences("LastSleepState", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putString(contentType, content).apply()
}

fun loadLastSleepState(context: Context, contentType: String): String? {
    val sharedPref = context.getSharedPreferences("LastSleepState", Context.MODE_PRIVATE)
    return sharedPref.getString(contentType, "")
}

fun String.parseLocation(): Triple<Int, Double?, Double?>{
    return this.split(",").let{
        Triple(it[0].toInt(), it[1].toDoubleOrNull(), it[2].toDoubleOrNull())
    }
}

// For test (現在使用されてない). 各項目に10万件ログを入れてどうなるか
private fun insertBigRecords(viewModel: MainViewModel) {
    val tabData = listOf("場所","睡眠", "アプリ")
    tabData.forEach { tabText ->
        repeat(100000) {
            insertTimeLog(
                TimeLog(
                    contentType = tabText,
                    timeContent = "",
                    fromDateTime = LocalDateTime.of(2022,1,1,1,1),
                    untilDateTime = LocalDateTime.of(2022,1,1,2,1)
                ), viewModel
            )
        }
    }
}
