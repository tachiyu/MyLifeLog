package com.example.timeitforward.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.example.timeitforward.ActivityTransitionManager
import com.example.timeitforward.MainViewModel
import com.example.timeitforward.model.db.transition.Transition

@Composable
fun SettingScreenSetup(viewModel: MainViewModel) {
    val activityTransitionManager = ActivityTransitionManager.getInstance(LocalContext.current)
    val allTransitions by viewModel.allTransitions.observeAsState(listOf())
    SettingScreen(
        onClick1 = {activityTransitionManager.startActivityUpdate()},
        onClick2 = {activityTransitionManager.stopActivityUpdate()},
        allTransitions
        )
}

@Composable
fun SettingScreen(onClick1: ()->Unit, onClick2: () -> Unit, allTransitions: List<Transition>) {
    Column {
        Button(
            onClick = onClick1
        ){ Text(text = "アクティビティの記録を開始する")}
        Button(
            onClick = onClick2
        ){ Text(text = "アクティビティの記録を終了する")}
        LazyColumn(content = {
            items(allTransitions){ item -> 
                Column(){
                    Text(text = "${item.activityType}  ${item.transitionType}")
                    Text(text = "${item.dateTime}")
                    Text(text = "${item.elapsedTimeNano}")
                }
            }
        })
    }
}