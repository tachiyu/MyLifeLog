package com.example.timeitforward

import android.content.Context
import android.util.Log
import com.example.timeitforward.model.db.sleep.Sleep
import com.example.timeitforward.model.db.timelog.TimeLog
import java.time.LocalDateTime

class SleepLogManager(private val activity: MainActivity, private val viewModel: MainViewModel) {
    companion object {
        private val TAG = SleepLogManager::class.java.simpleName
        private const val CONTENT_TYPE = "sleep"
    }

    fun updateSleepLogs(){
        Log.d(TAG, "updateSleepLogs called")
        val lastUpdateTime: LocalDateTime = loadLastUpdateTime(activity as Context, CONTENT_TYPE)
        val dateTimeNow : LocalDateTime = LocalDateTime.now()
        val lastSleepState: String = loadLastSleepState(activity as Context, CONTENT_TYPE)!!
        val sleepEvents: List<Sleep> =
            viewModel.getSleepBetween(lastUpdateTime, dateTimeNow)
        var timeContent = lastSleepState
        var fromDateTime = lastUpdateTime
        var untilDateTime = lastUpdateTime

        if (sleepEvents.isNotEmpty()) {
            Log.d(TAG, "There are ${sleepEvents.size} events")
            sleepEvents.forEach { event ->
                untilDateTime = event.dateTime
                insertTimeLog(
                    TimeLog(
                        contentType = CONTENT_TYPE,
                        timeContent = timeContent,
                        fromDateTime = fromDateTime,
                        untilDateTime = untilDateTime
                    ), viewModel)
                fromDateTime = event.dateTime
                timeContent = if (event.confidence >= 70 /*睡眠*/ ) {
                    "sleep"
                } else if (event.confidence <= 30 /*起床*/ ){
                    "awake"
                } else /*不明*/ {
                    "unsure"
                }
            }
            setLastUpdateTime(activity as Context, CONTENT_TYPE, untilDateTime)
            setLastSleepState(activity as Context, CONTENT_TYPE, timeContent)
        } else {
            Log.d(TAG, "sleepEvents is empty")
        }

    }
}