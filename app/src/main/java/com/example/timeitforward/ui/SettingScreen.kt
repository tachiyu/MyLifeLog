package com.example.timeitforward.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.timeitforward.MainViewModel
import com.example.timeitforward.model.apimanager.ActivityTransitionManager
import com.example.timeitforward.model.apimanager.SleepManager
import com.example.timeitforward.model.db.sleep.Sleep
import com.example.timeitforward.model.db.transition.Transition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity

private const val TAG = "SettingScreen"

@Composable
fun SettingScreenSetup(viewModel: MainViewModel) {
    val allTransitions by viewModel.allTransitions.observeAsState(listOf())
    val allSleeps by viewModel.allSleeps.observeAsState(listOf())
    SettingScreen(allTransitions, allSleeps)
}

@Composable
fun SettingScreen(allTransitions: List<Transition>, allSleep: List<Sleep>) {
    Column {
        ARSubscribeSwitch()
        SleepSubscribeSwitch()
        SendARBroadcastButton(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER)

        Row {
            val scrollStateLeft = rememberScrollState()
            LazyColumn(modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollStateLeft), content = {
                items(allTransitions){ item ->
                    Column(){
                        Text(text = "${item.activityType}  ${item.transitionType}")
                        Text(text = "${item.dateTime}")
                        Text(text = "${item.latitude} ${item.longitude}")
                        Text(text = "${item.elapsedTimeNano}")
                    }
                }
            })
            val scrollStateRight = rememberScrollState()
            LazyColumn(modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollStateRight), content = {
                items(allSleep){ item ->
                    Column(){
                        Text(text = "${item.confidence}")
                        Text(text = "${item.dateTime}")
                        Text(text = "${item.brightness}")
                        Text(text = "${item.motion}")
                    }
                }
            })
        }
    }
}

@Composable
fun SleepSubscribeSwitch(){
    // A switch to setting whether subscribe to Sleep API
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    val key = "IsSleepDetectionSubscribed"
    var checkedState by remember{ mutableStateOf(false) }
    if (!sharedPref.contains(key)) {
        sharedPrefEditor.putBoolean(key, false).apply()
    } else {
        checkedState = sharedPref.getBoolean(key, false)
    }
    val sleepManager = SleepManager.getInstance(context)
    Row() {
        Text("睡眠を検知する")
        Switch(checked = checkedState, onCheckedChange = {
            if(it) {
                sleepManager.startSleepUpdate()
                sharedPrefEditor.putBoolean(key, true).apply()
                checkedState = true
            } else {
                sleepManager.stopSleepUpdate()
                sharedPrefEditor.putBoolean(key, false).apply()
                checkedState = false
            }
        })
    }
}

@Composable
fun ARSubscribeSwitch(){
    // A switch to setting whether subscribe to Activity Recognition API
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    val key = "IsActivityRecognitionSubscribed"
    var checkedState by remember{ mutableStateOf(false) }
    if (!sharedPref.contains(key)) {
        sharedPrefEditor.putBoolean(key, false).apply()
    } else {
        checkedState = sharedPref.getBoolean(key, false)
    }
    val activityTransitionManager = ActivityTransitionManager.getInstance(context)
    Row() {
        Text("アクティビティの変化を検知する")
        Switch(checked = checkedState, onCheckedChange = {
            if(it) {
                activityTransitionManager.startActivityUpdate()
                sharedPrefEditor.putBoolean(key, true).apply()
                checkedState = true
            } else {
                activityTransitionManager.stopActivityUpdate()
                sharedPrefEditor.putBoolean(key, false).apply()
                checkedState = false
            }
        })
    }
}

@Composable
fun SendARBroadcastButton(activityType:Int, transitionType:Int){
    //A button to send Activity Recognition broadcast to ActivityUpdatesBroadcastReceiver for tests.
    val activityTransitionManager = ActivityTransitionManager.getInstance(LocalContext.current)
    var dropDown1Expanded by remember { mutableStateOf(false) }
    var selected1 by remember { mutableStateOf(0) }
    var dropDown2Expanded by remember { mutableStateOf(false) }
    var selected2 by remember { mutableStateOf(0) }

    Row(){
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.clickable(onClick = {dropDown1Expanded = true})){
                Text(selected1.toString())
                DropdownMenu(expanded = dropDown1Expanded, onDismissRequest = { dropDown1Expanded = false }) {
                    ActivityTransitionManager.activities.forEach {
                        DropdownMenuItem(onClick = {
                            dropDown1Expanded = false
                            selected1 = it
                        }) { Text(it.toString()) }
                    }
                }
            }
            Box(modifier = Modifier.clickable(onClick = {dropDown2Expanded = true})){
                Text(selected2.toString())
                DropdownMenu(expanded = dropDown2Expanded, onDismissRequest = { dropDown2Expanded = false }) {
                    (0..1).forEach {
                        DropdownMenuItem(onClick = {
                            dropDown2Expanded = false
                            selected2 = it
                        }) { Text(it.toString()) }
                    }
                }
            }
        }
        Button(modifier = Modifier.weight(1f),
            onClick = {activityTransitionManager.sendBroadcastForTest(selected1, selected2)}) {
            Text(text = "アクティビティを送る")
        }
    }
}
