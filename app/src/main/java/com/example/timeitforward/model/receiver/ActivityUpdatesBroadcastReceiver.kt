package com.example.timeitforward.model.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.timeitforward.model.db.MainRoomDatabase
import com.example.timeitforward.model.db.transition.Transition
import com.example.timeitforward.model.db.transition.TransitionRepository
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.time.LocalDateTime

private const val TAG = "ActivityUpdatesBroadcastReceiver"

class ActivityUpdatesBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() context:$context, intent:$intent")
        if (ActivityTransitionResult.hasResult(intent)) {
            Log.d(TAG, "received result")
            val result = ActivityTransitionResult.extractResult(intent)!!
            val dB: MainRoomDatabase = MainRoomDatabase.getInstance(context)
            val transitionRepository = TransitionRepository(dB.TransitionDao())
            var locationClient = LocationServices.getFusedLocationProviderClient(context)

            for (event in result.transitionEvents) {
                //少なくとも1つパーミッションがあれば、Locationを取得。
                if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED )
                ) {
                    Log.d(TAG, "try to get location")
                    locationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        object : CancellationToken() {
                            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                            override fun isCancellationRequested() = false
                        }
                    ).addOnSuccessListener { location ->
                        Log.d(TAG, "get location")
                        transitionRepository.insertTransition(
                            Transition(
                                activityType = event.activityType,
                                transitionType = event.transitionType,
                                dateTime = LocalDateTime.now(),
                                elapsedTimeNano = event.elapsedRealTimeNanos,
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    }.addOnFailureListener {
                        Log.d(TAG, "can not get location")
                        transitionRepository.insertTransition(
                            Transition(
                                activityType = event.activityType,
                                transitionType = event.transitionType,
                                dateTime = LocalDateTime.now(),
                                elapsedTimeNano = event.elapsedRealTimeNanos,
                                latitude = null,
                                longitude = null
                            )
                        )
                    }
                } else {
                    Log.d(TAG, "location permission denied")
                    transitionRepository.insertTransition(
                        Transition(
                            activityType = event.activityType,
                            transitionType = event.transitionType,
                            dateTime = LocalDateTime.now(),
                            elapsedTimeNano = event.elapsedRealTimeNanos,
                            latitude = null,
                            longitude = null
                        )
                    )
                }
            }
        }
    }

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.example.timeitforward.action.PROCESS_UPDATES"
    }
}