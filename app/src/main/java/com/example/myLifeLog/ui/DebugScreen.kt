package com.example.myLifeLog.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myLifeLog.*

@Composable
fun DebugScreenSetup(viewModel: MainViewModel) {
    val allLocations = viewModel.getAllLocations()
    val allTransitions = viewModel.getAllTransitions()
    val allSleeps = viewModel.getAllSleeps()
    DebugScreen(
        allLocations,
        allTransitions,
        allSleeps,
        readLog(context = LocalContext.current)
    )
}

@Composable
fun DebugScreen(
    allLocations: List<Location>,
    allTransitions: List<Transition>,
    allSleep: List<Sleep>,
    logs: List<String>
) {
    var dataType by remember{ mutableStateOf(0) }

    Column {
        when (dataType) {
            DataType.LOCATION -> {
                val scrollState = rememberScrollState()
                LazyColumn(modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState), content = {
                    items(allLocations.reversed()){ item ->
                        Column(){
                            Text(text = "id:${item.id} name:${item.name}")
                            Text(text = "   lat:${item.latitude} lon:${item.longitude}")
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
                        Column {
                            Text(text = "id:${item.id} type:${item.activityType} trans:${item.transitionType} locId:${item.locationId}")
                            Text(text = "   dateTime:${item.dateTime.toLocalDateTime()}")
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
                            Text(text = "id:${item.id} conf:${item.confidence}")
                            Text(text = "   dateTime:${item.dateTime.toLocalDateTime()}")
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