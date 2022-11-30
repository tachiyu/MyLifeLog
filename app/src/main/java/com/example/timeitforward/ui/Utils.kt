package com.example.timeitforward.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.timeitforward.R
import com.example.timeitforward.getAppName
import com.example.timeitforward.model.db.timelog.TimeLog
import java.time.LocalDateTime

// アプリの名前からアイコンイメージを取得
@Composable
fun AppIcon(
    appName: String,
    modifier: Modifier = Modifier,
    context: Context
) {
    val iconImage = try {
        context.packageManager.getApplicationIcon(appName).toBitmap().asImageBitmap()
    } catch(e: PackageManager.NameNotFoundException) { // getApplicationIconでappNameからIconを呼び出せなかったとき
        context.resources.getDrawable(R.drawable.null_app_icon).toBitmap().asImageBitmap() // 代替の白い円
    }
    Image(bitmap = iconImage,
        contentDescription = appName,
        modifier = modifier)
}

// 汎用的なタブバー
@Composable
fun TabBar(
    modifier: Modifier,
    tabIndex: Int,
    tabData: List<String>,
    onTabSwitch: (Int, String) -> Unit
) {
    TabRow(
        modifier = modifier,
        selectedTabIndex = tabIndex
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(id.toString(), modifier = Modifier.weight(0.1f))
        if (contentType == stringResource(id = R.string.app)) {
            AppIcon(appName = content, modifier = Modifier.size(40.dp, 40.dp), context = context)
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
