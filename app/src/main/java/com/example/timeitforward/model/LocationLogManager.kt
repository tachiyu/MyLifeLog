package com.example.timeitforward.model

import android.util.Log
import com.example.timeitforward.MainActivity
import com.example.timeitforward.MainViewModel
import com.example.timeitforward.R
import com.example.timeitforward.model.db.timelog.TimeLog
import com.example.timeitforward.model.db.transition.Transition
import com.example.timeitforward.toLocalDateTime
import com.google.android.gms.location.ActivityTransition
import java.time.LocalDateTime

class LocationLogManager(private val activity: MainActivity, private val viewModel: MainViewModel) {
    private val tag = LocationLogManager::class.java.simpleName
    private val contentType = activity.getString(R.string.location)

    fun loadLocationLogs(){
        Log.d(tag, "loadTransitionLog called")
        val lastSavedUntilDT: LocalDateTime = viewModel.getLastLogInContent(contentType)?.untilDateTime
            ?: 0L.toLocalDateTime()
        val transitionEvents: List<Transition> =
            viewModel.getTransitionBetween(lastSavedUntilDT, LocalDateTime.now())
        var fromDateTime: LocalDateTime = lastSavedUntilDT
        var untilDateTime: LocalDateTime

        if (transitionEvents.isNotEmpty()) {
            transitionEvents.forEach { event ->
                if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                    fromDateTime = event.dateTime
                } else {
                    untilDateTime = event.dateTime
                    viewModel.insertTimeLog(
                        TimeLog(contentType, "${event.activityType},${event.latitude},${event.longitude}",
                            fromDateTime, untilDateTime)
                    )
                }
            }
            val lastEvent = transitionEvents.last()
            if (lastEvent.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                viewModel.insertTimeLog(
                    TimeLog(contentType,
                        "${lastEvent.activityType},${lastEvent.latitude},${lastEvent.longitude}",
                        lastEvent.dateTime, LocalDateTime.now())
                )
            }
        } else {
            Log.d(tag, "transitionEvents is empty")
        }
    }
}