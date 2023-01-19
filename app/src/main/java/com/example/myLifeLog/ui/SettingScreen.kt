package com.example.myLifeLog.ui

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
import com.example.myLifeLog.*
import com.example.myLifeLog.model.apimanager.ActivityTransitionManager
import com.example.myLifeLog.model.db.location.Location
import com.example.myLifeLog.model.db.sleep.Sleep
import com.example.myLifeLog.model.db.timelog.TimeLog
import com.example.myLifeLog.model.db.transition.Transition
import java.time.LocalDateTime

private const val TAG = "SettingScreen"

@Composable
fun SettingScreenSetup(viewModel: MainViewModel) {
    val allTimeLogs by viewModel.allTimeLogs.observeAsState(listOf())
    val allLocations by viewModel.allLocations.observeAsState(listOf())
    val allTransitions by viewModel.allTransitions.observeAsState(listOf())
    val allSleeps by viewModel.allSleeps.observeAsState(listOf())
    SettingScreen(
        allTimeLogs,
        allLocations,
        allTransitions,
        allSleeps,
        { viewModel.updateLocationLogs() }
    )
}

@Composable
fun SettingScreen(
    allTimeLogs: List<TimeLog>,
    allLocations: List<Location>,
    allTransitions: List<Transition>,
    allSleep: List<Sleep>,
    updateLocationLogs: () -> Unit
) {
    var cTabIdx by remember{ mutableStateOf(0) }
    var dTabIdx by remember{ mutableStateOf(0) }

    Column() {
        SubscribeATSwitch(updateLocationLogs)
        SubscribeSleepSwitch()
        when (dTabIdx) {
            DB.LOCATION -> {
                val scrollState = rememberScrollState()
                LazyColumn(modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState), content = {
                    items(allLocations.reversed()){ item ->
                        Column(){
                            Text(text = "${item.id} ${item.name}")
                            Text(text = "   ${item.latitude} ${item.longitude}")
                        }
                    }
                })
            }
            DB.TRANSITION -> {
                val scrollState = rememberScrollState()
                LazyColumn(modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState), content = {
                    items(allTransitions.reversed()){ item ->
                        Column(){
                            Text(text = "${item.id} ${item.activityType} ${item.transitionType}")
                            Text(text = "   ${item.dateTime}")
                            Text(text = "   ${item.latitude} ${item.longitude}")
                        }
                    }
                })
            }
            else -> {
                val scrollState = rememberScrollState()
                LazyColumn(modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState), content = {
                    items(allSleep.reversed()){ item ->
                        Column(){
                            Text(text = "${item.id} ${item.confidence} ${item.brightness} ${item.motion}")
                            Text(text = "   ${item.dateTime}")
                        }
                    }
                })
            }
        }
        DbTabBar(
            modifier = Modifier,
            dBTabIndex = dTabIdx,
            onTabSwitch = {
                    index, _ -> dTabIdx = index
            }
        )
        val timeLogs = allTimeLogs.filter {
            it.contentType == when(cTabIdx) {
                CONTENT_TYPES.APP -> "app"
                CONTENT_TYPES.LOCATION -> "location"
                CONTENT_TYPES.SLEEP -> "sleep"
                else -> "others"
            }
        }
        val scrollState2 = rememberScrollState()
        LazyColumn(modifier = Modifier
            .weight(1f)
            .horizontalScroll(scrollState2), content = {
            items(timeLogs.reversed()){ item ->
                Column(){
                    Text(text = "${item.id} ${item.contentType}")
                    Text(text = "   ${item.timeContent}")
                    Text(text = "   ${item.fromDateTime} ${item.untilDateTime}")
                }
            }
        })
        ContentTabBar(
            modifier = Modifier,
            contentTabIndex = cTabIdx,
            onTabSwitch = {
                    index, _ -> cTabIdx = index
            }
        )

    }
}

@Composable
fun SubscribeSleepSwitch() {
    val context = LocalContext.current
    val key = "IsSleepDetectionSubscribed"
    var checkedState by remember { mutableStateOf(loadSetting(context, key)) }
    Row() {
        Text("睡眠を検知する")
        Switch(checked = checkedState, onCheckedChange = {
            if(it) {
                subscribeSleep(context)
                checkedState = true
            } else {
                stopSubscribeSleep(context)
                checkedState = false
            }
        })
    }
}

@Composable
fun SubscribeATSwitch(updateLocationLogs: () -> Unit) {
    val context = LocalContext.current
    val key = "IsActivityRecognitionSubscribed"
    var checkedState by remember { mutableStateOf(loadSetting(context, key)) }
    Row() {
        Text("アクティビティの変化を検知する")
        Switch(checked = checkedState, onCheckedChange = {
            if(it) {
                subscribeAT(context)
                checkedState = true
            } else {
                stopSubscribeAT(context, updateLocationLogs)
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
    }
}

@Composable
fun clearTransitionButton(viewModel: MainViewModel){
    Button(onClick = {viewModel.clearTransitionTable()}
    ){
        Text(text = "Transitionを消去")
    }
}

@Composable
fun clearSleepButton(viewModel: MainViewModel){
    Button(onClick = {viewModel.clearSleepTable()}
    ){
        Text(text = "Sleepを消去")
    }
}

@Composable
fun clearLocationButton(viewModel: MainViewModel){
    Button(onClick = {viewModel.clearContent("location")}
    ){
        Text(text = "LocationLogを消去")
    }
}

@Composable
fun clearAppButton(viewModel: MainViewModel){
    Button(onClick = {viewModel.clearContent("app")}
    ){
        Text(text = "AppLogを消去")
    }
}

@Composable
fun clearSleepLogButton(viewModel: MainViewModel){
    Button(onClick = {viewModel.clearContent("sleep")}
    ){
        Text(text = "SleepLogを消去")
    }
}

@Composable
fun setLastLocationUpdateButton(viewModel: MainViewModel){
    val context = LocalContext.current
    Button(onClick = {
        setLastUpdateTime(context, "location", LocalDateTime.of(2022, 12, 15, 23, 30,0))
        setLastLocation(context, "3,35.05408360791938,135.74253020215457")
    }
    ){
        Text(text = "reset LastLocation")
    }
}

@Composable
fun setLastSleepUpdateButton(viewModel: MainViewModel){
    val context = LocalContext.current
    Button(onClick = {
        setLastUpdateTime(context, "sleep", LocalDateTime.of(2022, 12, 14, 21, 0,0))
        setLastSleepState(context,"awake")
    }
    ){
        Text(text = "reset LastSleep")
    }
}