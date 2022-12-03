package com.example.timeitforward.model.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.timeitforward.model.db.MainRoomDatabase
import com.example.timeitforward.model.db.sleep.Sleep
import com.example.timeitforward.model.db.sleep.SleepRepository
import com.example.timeitforward.toLocalDateTime
import com.google.android.gms.location.SleepClassifyEvent

private const val TAG = "SleepBroadcastReceiver"

class SleepBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() context:$context, intent:$intent")
        if (SleepClassifyEvent.hasEvents(intent)) {
            Log.d(TAG, "received sleep event")
            val events = SleepClassifyEvent.extractEvents(intent)
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
            "com.example.timeitforward.action.PROCESS_UPDATES"
    }
}