package com.example.myLifeLog.model.apimanager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.myLifeLog.model.checkActivityPermission
import com.example.myLifeLog.model.receiver.SleepBroadcastReceiver
import com.example.myLifeLog.myLog
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.SleepSegmentRequest

private const val TAG = "SleepManager"

class SleepManager private constructor(private val context: Context){

    private val activityRecognitionClient: ActivityRecognitionClient =
        ActivityRecognition.getClient(context)

    private val sleepUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, SleepBroadcastReceiver::class.java)
        intent.action = SleepBroadcastReceiver.ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("MissingPermission")
    fun startSleepUpdate(){
        myLog(TAG, "startSleepUpdate called")
        if (!checkActivityPermission(context)) {
            myLog(TAG, "permission denied: ACTIVITY_RECOGNITION")
            return
        } else{
            activityRecognitionClient.requestSleepSegmentUpdates(
                sleepUpdatePendingIntent,
                SleepSegmentRequest(SleepSegmentRequest.CLASSIFY_EVENTS_ONLY)
            ).also {
                it.addOnSuccessListener { myLog(TAG, "subscribe to sleep detection") }
                it.addOnFailureListener { myLog(TAG, "can not subscribe to sleep detection") }
            }
        }
    }

    fun stopSleepUpdate() {
        myLog(TAG, "stopSleepUpdate called")
        if (!checkActivityPermission(context)) {
            myLog(TAG, "permission denied: ACTIVITY_RECOGNITION")
            return
        }
        activityRecognitionClient.removeSleepSegmentUpdates(sleepUpdatePendingIntent).also{
            it.addOnSuccessListener { myLog(TAG, "stop subscribe to sleep detection") }
            it.addOnFailureListener { myLog(TAG, "can not stop subscribe to sleep detection") }
        }
    }

    companion object {
        @Volatile private var INSTANCE: SleepManager? = null

        fun getInstance(context: Context): SleepManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SleepManager(context).also { INSTANCE = it }
            }
        }
    }
}
