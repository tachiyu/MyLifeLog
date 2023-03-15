package com.example.myLifeLog.model.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myLifeLog.doSomethingWithLocation
import com.example.myLifeLog.model.db.MainRoomDatabase
import com.example.myLifeLog.model.db.transition.Transition
import com.example.myLifeLog.model.db.transition.TransitionRepository
import com.example.myLifeLog.myLog
import com.example.myLifeLog.toMilliSec
import com.google.android.gms.location.*
import java.time.LocalDateTime

private const val TAG = "ActivityUpdatesBroadcastReceiver"

class ActivityUpdatesBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            myLog(TAG, "receive ${result.transitionEvents.size} transition events")
            val dB: MainRoomDatabase = MainRoomDatabase.getInstance(context)
            val transitionRepository = TransitionRepository(dB.TransitionDao())
            val locationClient = LocationServices.getFusedLocationProviderClient(context)

            for (event in result.transitionEvents) {
                myLog(TAG, "get event: ActivityType=${event.activityType}, TransitionType=${event.transitionType}")
                val dateTimeNow = LocalDateTime.now().toMilliSec()
                //ActivityがSTILL、TransitionがEnterならLocationを取得しつつinsertTransition。
                //Activity・Transitionがそれ以外か、位置のパーミッションがない場合は、Locationは取得しないでinsertTransition。
                if (
                    event.activityType == DetectedActivity.STILL &&
                    event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER
                ) {
                    doSomethingWithLocation(
                        context, locationClient,1, 5,
                        onSuccess = { location ->
                            transitionRepository.insertTransition(
                                Transition(
                                    activityType = event.activityType,
                                    transitionType = event.transitionType,
                                    dateTime = dateTimeNow,
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            )
                        },
                        onFailure = {
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
                    )
                } else {
                    myLog(TAG, "pushed without location info")
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
            "com.example.myLifeLog.action.TRANSITION_UPDATES"
    }
}