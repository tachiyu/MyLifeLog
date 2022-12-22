package com.example.timeitforward.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.timeitforward.R
import com.example.timeitforward.SleepLogManager
import com.example.timeitforward.getAppName
import com.example.timeitforward.model.AppLogManager
import com.example.timeitforward.model.LocationLogManager
import com.example.timeitforward.model.db.timelog.TimeLog
import com.google.android.gms.location.DetectedActivity
import java.time.LocalDateTime

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
    val resourceId = when(sleepState){
        "sleep" -> R.drawable.sleep
        "awake" -> R.drawable.awake
        "unsure" -> R.drawable.unsure
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

// 汎用的なタブバー
@Composable
fun TabBar(
    modifier: Modifier,
    tabIndex: Int,
    tabData: List<String>,
    onTabSwitch: (Int, String) -> Unit,
    backgroundColor: Color = MaterialTheme.colors.primary
) {
    TabRow(
        backgroundColor = backgroundColor,
        modifier = modifier,
        selectedTabIndex = tabIndex,
    ) {
        tabData.forEachIndexed { index, text ->
            Tab(selected = tabIndex == index,
                onClick = {
                    onTabSwitch(index, text)
                },
                text = { Text(text = text) })
        }
    }
}

@Composable
fun PeriodTabBar(modifier: Modifier, periodTabIndex: Int, onTabSwitch: (Int, String) -> Unit){
    TabBar(
        modifier = Modifier.height(56.dp),
        tabIndex = periodTabIndex,
        tabData = listOf(
            stringResource(id = R.string.month),
            stringResource(id = R.string.week),
            stringResource(id = R.string.day),),
        onTabSwitch = onTabSwitch,
        backgroundColor = MaterialTheme.colors.secondaryVariant
    )
}

@Composable
fun AppTabBar(modifier: Modifier, contentTabIndex: Int, onTabSwitch: (Int, String) -> Unit) {
    TabBar(
        modifier = modifier,
        tabIndex = contentTabIndex,
        tabData = contentTypesShown(),
        onTabSwitch = onTabSwitch,
        backgroundColor = MaterialTheme.colors.secondary
    )
}

@Composable
fun contentTypesShown(): List<String>{
    return listOf(
        stringResource(id = R.string.app),
        stringResource(id = R.string.location),
        stringResource(id = R.string.sleep),
        stringResource(id = R.string.others),
        )
}

//　TimeLogの情報を表示
@Composable
fun TimeLogRow(
    id: Int,
    contentType: String,
    content: String,
    fromDateTime: LocalDateTime,
    untilDateTime: LocalDateTime
) {
    val context: Context = LocalContext.current
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(5.dp)) {
        Text(id.toString(), modifier = Modifier.weight(0.1f))
        if (contentType == "app") {
            AppIcon(appName = content, modifier = Modifier.size(40.dp, 40.dp))
        } else {
            Text(contentType, modifier = Modifier.weight(0.1f))
        }
        Text(getAppName(content, context = context), modifier = Modifier.weight(0.2f))
        Text(text = fromDateTime.toString(), modifier = Modifier.weight(0.2f))
        Text(text = untilDateTime.toString(), modifier = Modifier.weight(0.2f))
    }
}

// TimeLogRowのlazy column
@Composable
fun TimeLogsLazyColumn(timeLogs: List<TimeLog>, modifier: Modifier=Modifier){
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        items(timeLogs) { item ->
            TimeLogRow(
                id = item.id,
                contentType = item.contentType,
                content = item.timeContent,
                fromDateTime = item.fromDateTime,
                untilDateTime = item.untilDateTime
            )
        }
    }
}

//TimeLogの更新ボタン
@Composable
fun UpdateButton(modifier: Modifier,
                 appLogManager: AppLogManager,
                 locationLogManager: LocationLogManager,
                 sleepLogManager: SleepLogManager
){
    Button(onClick = {
        appLogManager.updateAppLogs()
        locationLogManager.updateLocationLogs()
        sleepLogManager.updateSleepLogs()
    }) {
        Text(text = "更新する")
    }
}