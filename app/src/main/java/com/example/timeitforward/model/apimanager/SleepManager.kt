package com.example.timeitforward.model.apimanager

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.timeitforward.model.receiver.SleepBroadcastReceiver
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

    fun startSleepUpdate(){
        Log.d(TAG, "startSleepUpdate called")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "permission denied: ${Manifest.permission.ACTIVITY_RECOGNITION}")
            return
        } else{
            activityRecognitionClient.requestSleepSegmentUpdates(
                sleepUpdatePendingIntent,
                SleepSegmentRequest(SleepSegmentRequest.CLASSIFY_EVENTS_ONLY)
            ).also {
                it.addOnSuccessListener { Log.d(TAG, "subscribe to sleep detection") }
                it.addOnFailureListener { Log.d(TAG, "can not subscribe to sleep detection") }
            }
        }
    }

    fun stopSleepUpdate() {
        Log.d(TAG, "stopSleepUpdate called")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "permission denied: ${Manifest.permission.ACTIVITY_RECOGNITION}")
            return
        }
        activityRecognitionClient.removeSleepSegmentUpdates(sleepUpdatePendingIntent).also{
            it.addOnSuccessListener { Log.d(TAG, "stop subscribe to sleep detection") }
            it.addOnFailureListener { Log.d(TAG, "can not stop subscribe to sleep detection") }
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
