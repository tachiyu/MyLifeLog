package com.example.myLifeLog.model.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.myLifeLog.model.db.MainRoomDatabase
import com.example.myLifeLog.model.db.sleep.Sleep
import com.example.myLifeLog.model.db.sleep.SleepRepository
import com.example.myLifeLog.toLocalDateTime
import com.google.android.gms.location.SleepClassifyEvent

private const val TAG = "SleepBroadcastReceiver"

class SleepBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (SleepClassifyEvent.hasEvents(intent)) {
            val events = SleepClassifyEvent.extractEvents(intent)
            Log.d(TAG, "receive ${events.size} sleep events")
            val dB: MainRoomDatabase = MainRoomDatabase.getInstance(context)
            val sleepRepository = SleepRepository(dB.SleepDao())

            for (event in events) {
                sleepRepository.insertSleep(
                    Sleep(
                        confidence = event.confidence,
                        dateTime = event.timestampMillis.toLocalDateTime(),
                        brightness = event.light,
                        motion = event.motion
                    )
                )
            }
        }
    }

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.example.myLifeLog.action.SLEEP_UPDATES"
    }
}