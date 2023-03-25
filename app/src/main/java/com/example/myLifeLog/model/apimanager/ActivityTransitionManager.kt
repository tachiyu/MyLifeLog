package com.example.myLifeLog.model.apimanager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.myLifeLog.model.checkActivityPermission
import com.example.myLifeLog.model.receiver.ActivityUpdatesBroadcastReceiver
import com.example.myLifeLog.myLog
import com.google.android.gms.location.*

class ActivityTransitionManager  private constructor(private val context: Context){

    private val activityRecognitionClient: ActivityRecognitionClient =
        ActivityRecognition.getClient(context)

    private val transitions = mutableListOf<ActivityTransition>().apply {
        activities.forEach { a ->
            listOf(
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
                ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ).forEach { at ->
                    this.add(ActivityTransition.Builder()
                        .setActivityType(a)
                        .setActivityTransition(at)
                        .build()
                    )
            }
        }
    }

    private val activityUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, ActivityUpdatesBroadcastReceiver::class.java)
        intent.action = ActivityUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("MissingPermission")
    fun subscribeActivity(){
        if (!checkActivityPermission(context)) {
            myLog(TAG, "permission denied: ACTIVITY_RECOGNITION", context)
            return
        } else {
            activityRecognitionClient.requestActivityTransitionUpdates(
                ActivityTransitionRequest(transitions),
                activityUpdatePendingIntent
            ).also {
                it.addOnSuccessListener { myLog(TAG, "subscribe to activity detection", context) }
                it.addOnFailureListener { myLog(TAG, "can not subscribe to activity detection", context) }
            }
        }
    }

    companion object {
        @Volatile private var INSTANCE: ActivityTransitionManager? = null

        fun getInstance(context: Context): ActivityTransitionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ActivityTransitionManager(context).also { INSTANCE = it }
            }
        }

        val activities = listOf(
            DetectedActivity.STILL,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.RUNNING,
            DetectedActivity.WALKING
        )
        const val TAG = "ActivityTransitionManager"
    }
}