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
import com.example.timeitforward.*
import com.example.timeitforward.model.LocationLogManager
import com.example.timeitforward.model.apimanager.ActivityTransitionManager
import com.example.timeitforward.model.apimanager.SleepManager
import com.example.timeitforward.model.db.sleep.Sleep
import com.example.timeitforward.model.db.transition.Transition
import com.example.timeitforward.model.doSomethingWithLocation
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationServices
import java.time.LocalDateTime

private const val TAG = "SettingScreen"

@Composable
fun SettingScreenSetup(viewModel: MainViewModel) {
    val allTransitions by viewModel.allTransitions.observeAsState(listOf())
    val allSleeps by viewModel.allSleeps.observeAsState(listOf())
    val locationLogManager = LocationLogManager(LocalContext.current as MainActivity, viewModel)
    SettingScreen(allTransitions, allSleeps, locationLogManager, viewModel)
}

@Composable
fun SettingScreen(allTransitions: List<Transition>, 
                  allSleep: List<Sleep>, 
                  locationLogManager: LocationLogManager,
                  viewModel: MainViewModel
) {
    Column {
        ARSubscribeSwitch(locationLogManager)
        SleepSubscribeSwitch()
        SendARBroadcastButton(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        clearLocationButton(viewModel = viewModel)
        clearTransitionButton(viewModel = viewModel)
        clearAppButton(viewModel = viewModel)
        clearSleepLogButton(viewModel = viewModel)
        Row {setLastSleepUpdateButton(viewModel = viewModel)
            setLastLocationUpdateButton(viewModel = viewModel)
        }


        Row {
            val scrollStateLeft = rememberScrollState()
            LazyColumn(modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollStateLeft), content = {
                items(allTransitions.reversed()){ item ->
                    Column(){
                        Text(text = "${item.activityType}  ${item.transitionType}")
                        Text(text = "${item.dateTime}")
                        Text(text = "${item.latitude} ${item.longitude}")
                    }
                }
            })
            val scrollStateRight = rememberScrollState()
            LazyColumn(modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollStateRight), content = {
                items(allSleep.reversed()){ item ->
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
    val contentType = "sleep"
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
                setLastUpdateTime(context, contentType, LocalDateTime.now())
                setLastSleepState(context, contentType, "awake")
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
fun ARSubscribeSwitch(locationLogManager: LocationLogManager){
    // A switch to setting whether subscribe to Activity Recognition API
    val context = LocalContext.current
    val contentType = "location"
    val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    val sharedPrefEditor = sharedPref.edit()
    val key = "IsActivityRecognitionSubscribed"
    var checkedState by remember{ mutableStateOf(false) }
    //Settingから読み込んだkeyの状態をSwitchに反映する。keyがなければfalseにセットしておく。
    if (!sharedPref.contains(key)) {
        sharedPrefEditor.putBoolean(key, false).apply()
    } else {
        checkedState = sharedPref.getBoolean(key, false)
    }
    val activityTransitionManager = ActivityTransitionManager.getInstance(context)
    var locationClient = LocationServices.getFusedLocationProviderClient(context)
    Row() {
        Text("アクティビティの変化を検知する")
        Switch(checked = checkedState, onCheckedChange = {
            if(it) { /*スイッチON➔・ActivityRecognitionTransitionAPIにサブスクライブ
                                　・Settings：IsActivityRecognitionSubscribedをtrueに
                                 ・lastLocation・lastUpdateTimeを更新*/
                activityTransitionManager.startActivityUpdate()
                setLastUpdateTime(context, contentType, LocalDateTime.now())
                doSomethingWithLocation(TAG, context, locationClient,
                    onSuccess = {location -> setLastLocation(
                        context, contentType, "3,${location.latitude},${location.longitude}"
                    )},
                    onFailure = {setLastLocation(
                        context, contentType, "3,null,null"
                    )}
                )
                sharedPrefEditor.putBoolean(key, true).apply()
                checkedState = true
            } else { /*スイッチOFF➔・ActivityRecognitionTransitionAPIのサブスクライブを解除
                                　・Settings：IsActivityRecognitionSubscribedをfalseに
                                 ・updateLocationLogを実行*/
                activityTransitionManager.stopActivityUpdate()
                locationLogManager.updateLocationLogs()
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
        setLastLocation(context, "location", "3,35.05408360791938,135.74253020215457")
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
        setLastSleepState(context, "sleep", "awake")
    }
    ){
        Text(text = "reset LastSleep")
    }
}