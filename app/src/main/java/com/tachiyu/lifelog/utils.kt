package com.tachiyu.lifelog

import android.app.usage.UsageEvents
import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.google.android.gms.location.*
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

fun saveSharedPref(context: Context, key: String, long: Long) {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    sharedPrefEditor.putLong(key, long).apply()
}

fun loadSharedPrefLong(context: Context, key: String, defValue: Long = 0L): Long {
    val sharedPref = context.getSharedPreferences("AppSharedPref", Context.MODE_PRIVATE)
    return sharedPref.getLong(key, defValue)
}

fun logToLocal(tag: String, msg: String, time: LocalDateTime, context: Context) {
    val str = "${time.toString()}/ $tag/ $msg"
    val d = File(context.filesDir.path)
    if (!d.exists()) { d.mkdirs() }
    val f = File("${context.filesDir.path}/log.txt")
    val bw = BufferedWriter(FileWriter(f, true))
    bw.append(str)
    bw.newLine()
    bw.close()
}

fun myLog(tag: String, msg: String, context: Context) {
    Log.d(tag, msg)
    logToLocal(tag, msg, LocalDateTime.now(), context)
}

fun readLog(context: Context): List<String> {
    val br = BufferedReader(FileReader(File("${context.filesDir.path}/log.txt")))
    val lines =  br.readLines()
    br.close()
    return lines
}

data class ParsedTimeContent(
    val activityType: Int,
    val locationId: Int?
)

fun parseTimeContent(timeContent: String): ParsedTimeContent {
    return timeContent.split(",").let {
        ParsedTimeContent(it[0].toInt(), it[1].toIntOrNull())
    }
}

fun timeContent(activityType: Int, locationId: Int?): String {
    return "$activityType,$locationId"
}


// AppLogのリストをTimeLogのリストに変換
@JvmName("toTimeLogListAppLog")
fun List<App>.toTimeLogList(from: Long, until: Long): List<TimeLog> {
    val timeLogs = mutableListOf<TimeLog>()
    var tmpFromDateTime = from
    this.forEachIndexed { index, appLog ->
        when (appLog.eventType) {
            UsageEvents.Event.ACTIVITY_RESUMED -> {
                tmpFromDateTime = appLog.timeStamp
                if (index == this.lastIndex) {
                    timeLogs.add(
                        TimeLog(
                            timeContent = appLog.packageName,
                            fromDateTime = tmpFromDateTime,
                            untilDateTime = until
                        )
                    )
                }
            }
            UsageEvents.Event.ACTIVITY_PAUSED -> {
                timeLogs.add(
                    TimeLog(
                        timeContent = appLog.packageName,
                        fromDateTime = tmpFromDateTime,
                        untilDateTime = appLog.timeStamp
                    )
                )
            }
        }
    }
    return timeLogs
}

// TransitionのリストをTimeLogのリストに変換
fun List<Transition>.toTimeLogList(from: Long, until: Long): List<TimeLog> {
    val timeLogs = mutableListOf<TimeLog>()

    var currentFrom: Long? = null
    var currentActivity: Int? = null
    var currentTimeContent: String? = null
    var currentTransition: Int? = null

    for (i in this.indices) {
        // 現在のデータの状態と時刻を取得
        val transition = this[i]
        val transitionType = transition.transitionType
        val activityType = transition.activityType
        val locationId = transition.locationId
        val timePoint = transition.dateTime

        when (transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> {
                if (!(currentActivity == activityType && currentTransition == ActivityTransition.ACTIVITY_TRANSITION_ENTER)) {
                    currentTimeContent = timeContent(activityType, locationId)
                    currentFrom = maxOf(timePoint, from)
                }
                if (i == this.lastIndex) {
                    if (currentTimeContent != null && currentFrom != null) {
                        timeLogs.add(
                            TimeLog(
                                timeContent = currentTimeContent,
                                fromDateTime = currentFrom,
                                untilDateTime = until
                            )
                        )
                    }
                }
            }

            ActivityTransition.ACTIVITY_TRANSITION_EXIT ->
                if (currentActivity == activityType && timePoint >= from && i != 0) {
                    if (currentTimeContent != null && currentFrom != null) {
                        timeLogs.add(
                            TimeLog(
                                timeContent = currentTimeContent,
                                fromDateTime = currentFrom,
                                untilDateTime = minOf(timePoint, until)
                            )
                        )
                    }
                }
        }

        if (timePoint > until) {
            break
        }

        currentActivity = activityType
        currentTransition = transitionType
    }
    return timeLogs
}

@JvmName("toTimeLogListSleep")
fun List<Sleep>.toTimeLogList(from: Long, until: Long): List<TimeLog> {
    // TimeLogのリストを作成
    val timeLogList = mutableListOf<TimeLog>()

    // 睡眠状態を分類する関数
    val classifyState = { confidence: Int ->
        if (confidence > 70) SleepState.SLEEP
        else if (confidence < 30) SleepState.AWAKE
        else SleepState.UNKNOWN
    }

    // 現在の睡眠状態と開始時刻を初期化
    var currentSleepState: Int? = null
    var currentFrom: Long? = null

    // Sleepデータをループで処理
    for (i in this.indices) {
        // 現在のデータの睡眠状態と時刻を取得
        val sleepState = classifyState(this[i].confidence)
        val timePoint = this[i].dateTime

        // indexが０の時、sleepState, currentFromを初期化する。
        if (i == 0) {
            currentSleepState = sleepState
            currentFrom = maxOf(from, timePoint)
        }

        // 指定範囲より前のデータ
        if (timePoint < from) {
            currentSleepState = sleepState
            continue
        }

        // 睡眠状態が変わった場合、新しいTimeLogをリストに追加
        if (sleepState != currentSleepState) {
            timeLogList.add(
                TimeLog(
                    timeContent = currentSleepState.toString(),
                    fromDateTime = currentFrom!!,
                    untilDateTime = minOf(timePoint, until)
                )
            )
            currentFrom = timePoint
        }

        // 現在の睡眠状態を更新
        currentSleepState = sleepState

        // 最後のデータ、または指定範囲を超えた場合、最後のTimeLogをリストに追加
        if (i == this.lastIndex && timePoint < until) {
            timeLogList.add(
                TimeLog(
                    timeContent = currentSleepState.toString(),
                    fromDateTime = currentFrom!!,
                    untilDateTime = until
                )
            )
        }

        // 指定範囲を超えた場合、ループを終了
        if (timePoint >= until) {
            break
        }
    }

    // TimeLogのリストを返す
    return timeLogList
}

@JvmName("toTimeLogListOthers")
fun List<Others>.toTimeLogList(from: Long, until: Long): List<TimeLog> {
    val timeLogList = mutableListOf<TimeLog>()
    for (i in this.indices) {
        val log = this[i]
        if (log.untilDateTime < from || log.fromDateTime > until) {
            continue
        }

        val fromDateTime = maxOf(from, log.fromDateTime)
        val untilDateTime = minOf(until, log.untilDateTime)

        timeLogList.add(
            TimeLog(
                timeContent =  log.timeContent,
                fromDateTime = fromDateTime,
                untilDateTime = untilDateTime
            )
        )
    }
    return timeLogList
}



