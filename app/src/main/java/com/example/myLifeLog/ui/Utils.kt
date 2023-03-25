package com.example.myLifeLog.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.myLifeLog.*
import com.example.myLifeLog.R
import com.google.android.gms.location.DetectedActivity

// アプリの名前からアイコンイメージを取得
@Composable
fun AppIcon(appName: String, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconImage = try {
        context.packageManager.getApplicationIcon(appName).toBitmap().asImageBitmap()
    } catch(e: PackageManager.NameNotFoundException) { // getApplicationIconでappNameからIconを呼び出せなかったとき
        context.resources.getDrawable(R.drawable.null_app_icon).toBitmap().asImageBitmap() // 代替の白い円
    }
    Image(bitmap = iconImage,
        contentDescription = appName,
        modifier = modifier)
}

@Composable
fun ActivityIcon(activityType: Int, modifier: Modifier = Modifier) {
        val resourceId = when(activityType){
            DetectedActivity.STILL -> R.drawable.house
            DetectedActivity.WALKING -> R.drawable.walking
            DetectedActivity.RUNNING -> R.drawable.running
            DetectedActivity.ON_BICYCLE -> R.drawable.bycycle
            DetectedActivity.IN_VEHICLE -> R.drawable.automobile
            else -> R.drawable.null_app_icon
        }
    Image(modifier = modifier, painter = painterResource(id = resourceId), contentDescription = activityType.toString())
}

@Composable
fun SleepIcon(sleepState: String, modifier: Modifier = Modifier) {
    val resourceId = when(sleepState.toInt()){
        SleepState.SLEEP -> R.drawable.sleep
        SleepState.AWAKE -> R.drawable.awake
        SleepState.UNKNOWN -> R.drawable.unsure
        else -> R.drawable.null_app_icon
    }
    Image(modifier = modifier, painter = painterResource(id = resourceId), contentDescription = sleepState)
}

@Composable
fun OthersIcon(modifier: Modifier = Modifier
) {
    val iconImage = painterResource(id = R.drawable.others)
    Image(painter = iconImage,
        contentDescription = "others",
        modifier = modifier)
}

@Composable
fun PeriodTabBar(modifier: Modifier = Modifier, period: Int, onTabSwitch: (Int) -> Unit){
    TabRow(
        modifier = modifier,
        selectedTabIndex = period,
        backgroundColor = Color.White
    ) {
        Period.All.forEach { period2 ->
            Tab(
                selected = period == period2,
                onClick = { onTabSwitch(period2) },
                text = {
                    Text(
                        text = stringResource(id = Period.getStringId(period2)),
                        color = if (period == period2) {
                            Color.Black
                        } else {
                            Color.Gray
                        }
                    )
                },
                unselectedContentColor = Color.White
            )
        }
    }
}

@Composable
fun ContentTypeTabBar(modifier: Modifier = Modifier,
                      contentType: Int,
                      onTabSwitch: (Int) -> Unit) {
    TabRow(
        modifier = modifier,
        selectedTabIndex = ContentType.All.indexOf(contentType),
        backgroundColor = Color.White
    ) {
        ContentType.All.forEach { contentType2 ->
            Tab(
                selected = contentType == contentType2,
                onClick = { onTabSwitch(contentType2) },
                text = {
                    Text(
                        text = stringResource(id = ContentType.getStringId(contentType2)),
                        color = if (contentType == contentType2) { Color.Black } else { Color.Gray }
                    )
                       },
                icon = {
                    Image(
                        modifier = modifier.size(20.dp, 20.dp),
                        alpha = if (contentType == contentType2) { 1f } else { .5f },
                        painter = painterResource(id = ContentType.getIconId(contentType2)),
                        contentDescription = stringResource(id = ContentType.getStringId(contentType2))
                    )
                },
                unselectedContentColor = Color.White
            )
        }
    }
}

@Composable
fun DataTabBar(modifier: Modifier = Modifier, dataType: Int, onTabSwitch: (Int) -> Unit){
    TabRow(
        modifier = modifier,
        selectedTabIndex = dataType,
        backgroundColor = Color.White
    ) {
        DataType.All.forEach { dataType2 ->
            Tab(
                selected = dataType == dataType2,
                onClick = { onTabSwitch(dataType2) },
                text = {
                    Text(
                        text = DataType.getString(dataType2),
                        color = if (dataType2 == dataType) { Color.Black } else { Color.Gray }
                    )
                },
                unselectedContentColor = Color.White
            )
        }
    }
}
