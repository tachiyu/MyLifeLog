package com.example.timeitforward.model.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.timeitforward.model.db.MainRoomDatabase
import com.example.timeitforward.model.db.transition.Transition
import com.example.timeitforward.model.db.transition.TransitionRepository
import com.example.timeitforward.model.doSomethingWithLocation
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationServices
import java.time.LocalDateTime

private const val TAG = "ActivityUpdatesBroadcastReceiver"

class ActivityUpdatesBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            Log.d(TAG, "receive ${result.transitionEvents.size} transition events")
            val dB: MainRoomDatabase = MainRoomDatabase.getInstance(context)
            val transitionRepository = TransitionRepository(dB.TransitionDao())
            var locationClient = LocationServices.getFusedLocationProviderClient(context)

            for (event in result.transitionEvents) {
                Log.d(TAG, "get event: ActivityType=${event.activityType}, TransitionType=${event.transitionType}")
                val dateTimeNow = LocalDateTime.now()
                //ActivityがSTILL、TransitionがEnterならLocationを取得しつつinsertTransition。
                //Activity・Transitionがそれ以外か、位置のパーミッションがない場合は、Locationは取得しないでinsertTransition。
                if (
                    event.activityType == DetectedActivity.STILL &&
                    event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER
                ) {
                    doSomethingWithLocation(
                        TAG, context, locationClient,
                        onSuccess = { location ->
                            Log.d(TAG, "latitude:${location.latitude}, longitude:${location.longitude}")
                            transitionRepository.insertTransition(
                                Transition(
                                    activityType = event.activityType,
                                    transitionType = event.transitionType,
                                    dateTime = dateTimeNow,
                                    latitude = location.latitude,
                                    longitude = location.longitude)) },
                        onFailure = {
                            transitionRepository.insertTransition(
                                Transition(
                                    activityType = event.activityType,
                                    transitionType = event.transitionType,
                                    dateTime = dateTimeNow,
                                    latitude = null,
                                    longitude = null)) })
                } else {
                    Log.d(TAG, "pushed without location info")
                    transitionRepository.insertTransition(
                        Transition(
                            activityType = event.activityType,
                            transitionType = event.transitionType,
                            dateTime = dateTimeNow,
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
            "com.example.timeitforward.action.TRANSITION_UPDATES"
    }
}