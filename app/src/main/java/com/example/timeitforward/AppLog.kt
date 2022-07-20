package com.example.timeitforward

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// アプリ起動時or更新ボタン押下時にアプリのログをとってきてinsertTimeLogでデータベースに保存する。
class AppLog(private val activity: MainActivity) {

    private val tag = AppLog::class.java.simpleName

    private fun getUsageEventsObject(): UsageEvents {
        val usageStatsManager: UsageStatsManager =
            activity.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(2022, 6, 12)

        return usageStatsManager.queryEvents(
            calendar.timeInMillis,
            System.currentTimeMillis()
        )
    }

    private fun getUsageStatsObject(): List<UsageStats> {
        val usageStatsManager: UsageStatsManager =
            activity.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(2022, 6, 12)

        Log.d(
            tag,
            "${getStringDate(calendar.timeInMillis)} ${getStringDate(System.currentTimeMillis())}"
        )

        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            calendar.timeInMillis,
            System.currentTimeMillis()
        )
    }

    fun readUsageEvents() {
        val usageEvents: UsageEvents = getUsageEventsObject()
        Log.d(tag, "readUsageEvents called")
        var cnt = 0
        while (usageEvents.hasNextEvent()) {
            val event: UsageEvents.Event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            if (event.eventType in arrayOf(1, 2) && "launcher" !in event.packageName) {
                Log.d(tag, "Event $cnt")
                Log.d(
                    tag, "packageName: " + event.packageName +
                            "\neventType: " + when (event.eventType) {
                        1 -> "Move to ForeGround"
                        2 -> "Move to BackGround"
                        else -> {}
                    } +
                            "\ntimeStamp: " + getStringDate(event.timeStamp)
                )
                ++cnt
            }
        }
    }

    fun readDayUsageStats() {
        val usageStats = getUsageStatsObject()
        Log.d(tag, "func called")
        for (usageStat in usageStats) {
            Log.d(tag, "usage")
            if (usageStat.totalTimeInForeground == 0L) {
                continue
            }

            Log.d(
                tag, "packageName: " + usageStat.packageName +
                        "\ttotalTimeDisplayed: " + usageStat.totalTimeInForeground +
                        "\tfirstTime: " + getStringDate(usageStat.firstTimeStamp) +
                        "\tlastTime: " + getStringDate(usageStat.lastTimeStamp) +
                        "\tlastTimeUsed: " + getStringDate(usageStat.lastTimeUsed) +
                        "\tlastTimeUsed: " + getStringDate(usageStat.lastTimeUsed) +
                        "\tlastTimeUsed: " + getStringDate(usageStat.lastTimeUsed)
            )
        }
    }


    private fun getStringDate(milliseconds: Long): String {
        val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPANESE)
        val date = Date(milliseconds)
        return df.format(date)
    }

    fun readUsageEventsAsTimeLogs() {
        val usageEvents: UsageEvents = getUsageEventsObject()
        Log.d(tag, "readUsageEvents called")
        var isForegruond = false
        val contentType: String = R.string.app.toString()
        var timeContent = ""
        var fromDateTime: LocalDateTime = LocalDateTime.MIN
        var untilDateTime: LocalDateTime
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd H:m:s")

        while (usageEvents.hasNextEvent()) {
            val event: UsageEvents.Event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            if ("launcher" !in event.packageName) {
                if (event.eventType==UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (!isForegruond) {
                        timeContent = event.packageName
                        fromDateTime = LocalDateTime.parse(getStringDate(event.timeStamp), dateTimeFormatter)
                    } else {
                        if (timeContent != event.packageName) {
                            Log.e(tag, "timeContent miss match $timeContent, ${event.packageName}")
                        }
                    }
                    isForegruond = true
                } else if (event.eventType==UsageEvents.Event.ACTIVITY_PAUSED){
                    if (!isForegruond) {
                        Log.e(tag, "double background")
                    } else {
                        if (timeContent != event.packageName) {
                            Log.e(tag, "timeContent miss match $timeContent, ${event.packageName}")
                        }
                        untilDateTime = LocalDateTime.parse(getStringDate(event.timeStamp), dateTimeFormatter)
                        Log.d(tag, "contentType: $contentType" +
                                        "\ttimeContent: $timeContent" +
                                        "\tfromDateTime: $fromDateTime" +
                                        "\tuntilDateTime: $untilDateTime"
                        )
                    }
                    isForegruond = false
                }
            }
        }
    }

    fun loadAppLogs(viewModel: TimeLogViewModel) {

//* TODO
//
//
        insertTimeLog(
            contentType = R.string.app.toString(),
            timeContent = "", // アプリの名前
            fromDateTime = LocalDateTime.MIN, // 開始時間
            untilDateTime = LocalDateTime.MIN,// 終了時間
            viewModel = viewModel
        )
    }

    fun getLastTimeLogged(viewModel: TimeLogViewModel) {
    }
}
