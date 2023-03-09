package com.example.myLifeLog.ui

import androidx.compose.foundation.horizontalScroll
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
import com.example.myLifeLog.model.db.location.Location
import com.example.myLifeLog.model.db.sleep.Sleep
import com.example.myLifeLog.model.db.timelog.TimeLog
import com.example.myLifeLog.model.db.transition.Transition

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
        readLog(),
        { viewModel.updateLocationLogs() }
    )
}

@Composable
fun SettingScreen(
    allTimeLogs: List<TimeLog>,
    allLocations: List<Location>,
    allTransitions: List<Transition>,
    allSleep: List<Sleep>,
    logs: List<String>,
    updateLocationLogs: () -> Unit
) {
    var contentType by remember{ mutableStateOf(0) }
    var dataType by remember{ mutableStateOf(0) }

    Column() {
        SubscribeATSwitch(updateLocationLogs)
        SubscribeSleepSwitch()
        when (dataType) {
            DataType.LOCATION -> {
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
            DataType.TRANSITION -> {
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
        DataTabBar(
            modifier = Modifier,
            dataType = dataType,
            onTabSwitch = { dataType2, -> dataType = dataType2 }
        )
        val timeLogs = allTimeLogs.filter {
            it.contentType == contentType
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
        ContentTypeTabBar(
            modifier = Modifier,
            contentType = contentType,
            onTabSwitch = { index -> contentType = index }
        )
        Text("Log")
        LazyColumn(modifier = Modifier
            .weight(1f),
            content = {
            items(logs.reversed()) { item ->
                Text(item)
                }
            }
        )
    }
}

@Composable
fun SubscribeSleepSwitch() {
    val context = LocalContext.current
    var checkedState by remember {
        mutableStateOf(
            loadSharedPrefBool(
                context,
                "IsSleepDetectionSubscribed"
            )
        )
    }
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
    var checkedState by remember { mutableStateOf(
        loadSharedPrefBool(
            context,
            "IsActivityRecognitionSubscribed"
        )
    ) }
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