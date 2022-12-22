package com.example.timeitforward.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.timeitforward.*
import com.example.timeitforward.model.db.timelog.TimeLog
import com.example.timeitforward.model.db.transition.Transition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.time.LocalDateTime

class LocationLogManager(private val activity: MainActivity, private val viewModel: MainViewModel) {
    companion object {
        private val TAG = LocationLogManager::class.java.simpleName
        private const val CONTENT_TYPE = "location"
    }

    fun updateLocationLogs(){
        Log.d(TAG, "updateLocationLogs called")
        val lastUpdateTime: LocalDateTime = loadLastUpdateTime(activity as Context, CONTENT_TYPE)
        if (lastUpdateTime==0L.toLocalDateTime()){
            Log.d(TAG, "Error! there is no last update time to retrieve")
            return
        }
        val lastLocation: String = loadLastLocation(activity as Context, CONTENT_TYPE)!!
        val dateTimeNow : LocalDateTime = LocalDateTime.now()
        val transitionEvents: List<Transition> =
            viewModel.getTransitionBetween(lastUpdateTime, dateTimeNow)

        var timeContent = lastLocation
        var fromDateTime = lastUpdateTime
        var untilDateTime = dateTimeNow

        if (loadSetting(activity as Context, "IsActivityRecognitionSubscribed") == true) {
            if (transitionEvents.isNotEmpty()) {
                transitionEvents.forEach { event ->
                    when (event.transitionType) {
                        ActivityTransition.ACTIVITY_TRANSITION_ENTER -> {
                            fromDateTime = event.dateTime
                            timeContent = if (event.activityType == DetectedActivity.STILL) {
                                "${event.activityType},${event.latitude},${event.longitude}"
                            } else {
                                "${event.activityType},null,null"
                            }
                        }
                        ActivityTransition.ACTIVITY_TRANSITION_EXIT -> {
                            untilDateTime = event.dateTime
                            val (lastActivity, lastLatitude, lastLongitude) = timeContent.parseLocation()
                            if (event.activityType != lastActivity) {
                                timeContent = if (event.activityType == DetectedActivity.STILL) {
                                    "${event.activityType},${event.latitude},${event.longitude}"
                                } else {
                                    "${event.activityType},null,null"
                                }
                            }
                            insertTimeLog(
                                TimeLog(CONTENT_TYPE, timeContent, fromDateTime, untilDateTime), viewModel
                            )
                        }
                        else -> { error("ActivityTransition.XXX is invalid (either 0 or 1)") }
                    }
                }
                untilDateTime = dateTimeNow
                insertTimeLog(
                    TimeLog(CONTENT_TYPE, timeContent, fromDateTime, untilDateTime), viewModel
                )
                setLastUpdateTime(activity as Context, CONTENT_TYPE, dateTimeNow)
                setLastLocation(activity as Context, CONTENT_TYPE, timeContent)
            } else {
                Log.d(TAG, "transitionEvents is empty")
                setLastUpdateTime(activity as Context, CONTENT_TYPE, dateTimeNow)
                insertTimeLog(
                    TimeLog(CONTENT_TYPE, timeContent, fromDateTime, untilDateTime), viewModel
                )
            }
        } else {
            Log.d(TAG, "subscription to Activity Recognition is nothing")
        }
    }
}

fun doSomethingWithLocation(TAG: String,
                            context: Context,
                            locationClient: FusedLocationProviderClient,
                            onSuccess: (Location) -> Unit,
                            onFailure: () -> Unit
){
    // check permission
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
    ) {
        Log.d(TAG, "try to get location")
        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }
        ).addOnSuccessListener { location ->
            Log.d(TAG, "get location")
            if (location != null) {
                onSuccess(location)
            } else {
                onFailure()
            }
        }.addOnFailureListener {
            Log.d(TAG, "can not get location")
            onFailure()
        }
    } else {
        Log.d(TAG, "permission denied (either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION is required)")
        return
    }
}