package com.example.timeitforward

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.time.LocalDateTime

// アプリ起動時or更新ボタン押下時にアプリのログをとってきてinsertTimeLogでデータベースに保存する。
class AppLog(private val activity: MainActivity, private val viewModel: TimeLogViewModel) {

    private val tag = AppLog::class.java.simpleName
    private val app = activity.getString(R.string.app)

    // getLastUpdatedTimeから最新までのAppログを取得しRoomに保存する
    fun loadAppLogs() {
        Log.d(tag, "loadAppLogs called")
        val usageEvents: UsageEvents = getUsageEventsObject(startTime = getLastUpdatedTime())
        var isForeground = false

        val contentType: String = app
        var timeContent = ""
        var fromDateTime: LocalDateTime = convertMillisecondsToLocalDateTime(0) //初期値は使わないがコンパイルエラー回避
        var untilDateTime: LocalDateTime

        while (usageEvents.hasNextEvent()) {
            val event: UsageEvents.Event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            if ("launcher" !in event.packageName) {
                if (event.eventType==UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (isForeground) {
                        if (timeContent != event.packageName) {
                            Log.e(tag, "timeContent miss match $timeContent, ${event.packageName}")
                        }
                    } else {
                        timeContent = event.packageName
                        fromDateTime = convertMillisecondsToLocalDateTime(event.timeStamp)
                    }
                    isForeground = true
                } else if (event.eventType==UsageEvents.Event.ACTIVITY_PAUSED){
                    if (isForeground) {
                        if (timeContent != event.packageName) {
                          Log.e(tag, "timeContent miss match $timeContent, ${event.packageName}")
                        }
                        untilDateTime = convertMillisecondsToLocalDateTime(event.timeStamp)
                        insertTimeLog(
                            contentType = contentType,
                            timeContent = timeContent,
                            fromDateTime = fromDateTime, // 開始時間
                            untilDateTime = untilDateTime,// 終了時間
                            viewModel = viewModel
                        )
                    } else {
                        Log.e(tag, "double background")
                    }
                    isForeground = false
                }
            }
        }
    }

    // Roomに保存されているAppログの中で最新のログのバックグラウンド移動時刻を取得
    private fun getLastUpdatedTime(): LocalDateTime {
        val lastAppLog =  viewModel.getLastAppLog(app)
        Log.d(tag, "app: $app")
        val ret = if (lastAppLog.isNotEmpty()) {
            lastAppLog[0].untilDateTime
        } else {
            convertMillisecondsToLocalDateTime(0)
        }
        Log.d(tag, "getLastUpdatedTime: $ret")
        return ret
    }

    // UsageEventsオブジェクトを取得する
    private fun getUsageEventsObject(startTime: LocalDateTime): UsageEvents {
        val usageStatsManager: UsageStatsManager =
            activity.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        return usageStatsManager.queryEvents(
            convertLocalDateTimeToMilliseconds(startTime),
            System.currentTimeMillis()
        )
    }
}
