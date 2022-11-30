package com.example.timeitforward

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.timeitforward.model.db.MainRoomDatabase
import com.example.timeitforward.model.db.transition.Transition
import com.example.timeitforward.model.db.transition.TransitionRepository
import com.google.android.gms.location.ActivityTransitionResult
import java.time.LocalDateTime

private const val TAG = "ActivityUpdatesBroadcastReceiver"

class ActivityUpdatesBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() context:$context, intent:$intent")
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            val dB: MainRoomDatabase = MainRoomDatabase.getInstance(context)
            val transitionRepository = TransitionRepository(dB.TransitionDao())
            for (event in result.transitionEvents) {
                Toast.makeText(context, "$event", Toast.LENGTH_LONG).show()
                transitionRepository.insertTransition(
                    Transition(
                        activityType = event.activityType,
                        transitionType = event.transitionType,
                        dateTime = LocalDateTime.now(),
                        elapsedTimeNano = event.elapsedRealTimeNanos
                ))
            }
        }
    }

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.example.timeitforward.action.PROCESS_UPDATES"
    }
}