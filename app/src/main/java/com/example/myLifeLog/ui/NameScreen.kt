package com.example.myLifeLog.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myLifeLog.*
import com.example.myLifeLog.R
import com.example.myLifeLog.model.db.location.Location
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun NameScreen(
    viewModel: MainViewModel,
    navController: NavController,
    locId: Int,
    lat: Double,
    lon: Double,
    periodTabSelected: Int,
    contentTabSelected: Int
) {
    val nameLoc: (Location) -> Unit = { location -> viewModel.updateLocation(location) }
    val loc = LatLng(lat, lon)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(loc, 15f)
    }
    val navToSummary = {
        navController.navigate("${DESTINATIONS.SUMMARY.str}/$periodTabSelected,$contentTabSelected")
    }
    val backHandlingEnabled by remember { mutableStateOf(true) }
    BackHandler(backHandlingEnabled) {
        navToSummary()
    }
    Column(){
        GoogleMap(
            modifier = Modifier.size(300.dp, 300.dp),
            cameraPositionState = cameraPositionState
        ) {
            Marker(state = MarkerState(position = loc))
        }
        var name: String by remember { mutableStateOf("") }
        Row(Modifier.fillMaxWidth()){
            TextField(
                value = name,
                onValueChange = {name = it},
                label = { Text(stringResource(id = R.string.name_location)) },
                placeholder = { Text(stringResource(id = R.string.location_name_example)) },
                singleLine = true
            )
            Button(onClick = {
                nameLoc(Location(name, lat, lon).apply{ this.id = locId })
                navToSummary()
            }
            ) {
                Text(stringResource(id = R.string.OK))
            }
        }
    }
}

