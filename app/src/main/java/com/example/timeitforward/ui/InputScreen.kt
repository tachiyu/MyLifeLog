package com.example.timeitforward

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.timeitforward.data.db.TimeLog
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogButtons
import com.vanpra.composematerialdialogs.MaterialDialogScope
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.TimePickerColors
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@Composable
fun InputScreenSetup(viewModel: TimeLogViewModel) {
    val searchResults by viewModel.searchResults.observeAsState(listOf())

    InputScreen(
        searchResults = searchResults,
        viewModel = viewModel
    )
}


@Composable
fun InputScreen(
    searchResults: List<TimeLog>,
    viewModel: TimeLogViewModel
) {
    val tabData = listOf("場所", "アプリ", "睡眠", "その他")
    val dialogColor = remember { Color(0xFF3700B3) }

    var tabIndex by remember { mutableStateOf(0) }
    var contentType: String by remember { mutableStateOf("") }
    var timeContent: String by remember { mutableStateOf("") }
    var fromDate: LocalDate? by remember { mutableStateOf(null) }
    var untilDate: LocalDate? by remember { mutableStateOf(null) }
    var fromTime: LocalTime? by remember { mutableStateOf(null) }
    var untilTime: LocalTime? by remember { mutableStateOf(null) }

    updateSearchResults(
        text = tabData[tabIndex],
        viewModel = viewModel,
        tabData = tabData
    )

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = {
            loadLogs(viewModel)
            updateSearchResults(
                text = tabData[tabIndex],
                viewModel = viewModel,
                tabData = tabData
            )
        }) {
            Text("すべて更新")
        }
        TimeContentField(
            value = contentType,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            label = "種類を入力",
            onValueChange = { contentType = it })
        TimeContentField(
            value = timeContent,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            label = "内容を入力",
            onValueChange = { timeContent = it })
        Row {
            DatePickerDialogButton(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .background(MaterialTheme.colors.primaryVariant),
                buttonTitle = "何日から",
                dialogTitle = "何日から",
                onDateChange = { fromDate = it }
            )
            DatePickerDialogButton(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .background(MaterialTheme.colors.primaryVariant),
                buttonTitle = "何日まで",
                dialogTitle = "何日まで",
                onDateChange = { untilDate = it }
            )
        }
        Row {
            TimePickerDialogButton(
                dialogColor = dialogColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .background(MaterialTheme.colors.primaryVariant),
                buttonTitle = "何時から",
                dialogTitle = "何時から",
                onTimeChange = { fromTime = it }
            )
            TimePickerDialogButton(
                dialogColor = dialogColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .background(MaterialTheme.colors.primaryVariant),
                buttonTitle = "何時まで",
                dialogTitle = "何時まで",
                onTimeChange = { untilTime = it }
            )
        }
        TimeLogDraft(
            contentType = contentType,
            timeContent = timeContent,
            fromDate = fromDate,
            fromTime = fromTime,
            untilDate = untilDate,
            untilTime = untilTime
        )
        TextButton(
            modifier = Modifier
                .padding(8.dp)
                .background(MaterialTheme.colors.primaryVariant),
            onClick = {
                val fromDateTime = getLocalDateTime(fromDate, fromTime)
                val untilDateTime = getLocalDateTime(untilDate, untilTime)
                insertTimeLog(
                    contentType = contentType,
                    timeContent = timeContent,
                    fromDateTime = fromDateTime,
                    untilDateTime = untilDateTime,
                    viewModel = viewModel
                )
                updateSearchResults(
                    text = tabData[tabIndex],
                    viewModel = viewModel,
                    tabData = tabData
                )
            }
        ) {
            Text(
                "保存",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center),
                color = MaterialTheme.colors.onPrimary
            )
        }
        LazyColumn(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            items(searchResults) { item ->
                TimeLogRow(
                    id = item.id,
                    contentType = item.contentType,
                    content = item.timeContent,
                    fromDateTime = item.fromDateTime,
                    untilDateTime = item.untilDateTime
                )
            }
        }
        ContentTypeTabs(
            tabIndex = tabIndex,
            tabData = tabData,
            modifier = Modifier.weight(0.5f),
            onTabSwitch = { index, text ->
                tabIndex = index
                updateSearchResults(text, viewModel, tabData)
            }
        )

    }
}


private fun updateSearchResults(text: String, viewModel: TimeLogViewModel, tabData: List<String>) {
    if (text == "その他") {
        viewModel.findTimeLogByNotContentTypes(
            tabData.filter { tabText -> tabText != "その他" }
        )
    } else {
        viewModel.findTimeLogByContentType(text)
    }
}

@Composable
fun TimeContentField(
    value: String,
    label: String,
    modifier: Modifier,
    onValueChange: (String) -> Unit
) {
    TextField(
        value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun DatePickerDialogButton(
    modifier: Modifier,
    buttonTitle: String,
    dialogTitle: String,
    onDateChange: (LocalDate) -> Unit
) {
    DialogAndShowButton(
        modifier = modifier,
        buttonText = buttonTitle,
        buttons = { DefaultDateTimeDialogButtons() }
    ) {
        datepicker(
            title = dialogTitle,
            colors = DatePickerDefaults.colors(headerBackgroundColor = Color.Blue)
        ) {
            onDateChange(it)
        }
    }
}

@Composable
fun TimePickerDialogButton(
    dialogColor: Color,
    modifier: Modifier,
    buttonTitle: String,
    dialogTitle: String,
    onTimeChange: (LocalTime) -> Unit
) {
    val colors: TimePickerColors = getColors(dialogColor = dialogColor)
    DialogAndShowButton(
        modifier = modifier,
        buttonText = buttonTitle,
        buttons = { DefaultDateTimeDialogButtons() }
    ) {
        timepicker(
            title = dialogTitle,
            colors = colors,
            is24HourClock = true
        ) {
            onTimeChange(it)
        }
    }
}

@Composable
fun DialogAndShowButton(
    modifier: Modifier,
    buttonText: String,
    buttons: @Composable MaterialDialogButtons.() -> Unit = {},
    content: @Composable MaterialDialogScope.() -> Unit
) {
    val dialogState = rememberMaterialDialogState()

    MaterialDialog(dialogState = dialogState, buttons = buttons) {
        content()
    }

    TextButton(
        onClick = { dialogState.show() },
        modifier = modifier,
    ) {
        Text(
            buttonText,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center),
            color = MaterialTheme.colors.onPrimary
        )
    }
}

@Composable
fun TimeLogDraft(
    contentType: String, timeContent: String,
    fromDate: LocalDate?, fromTime: LocalTime?,
    untilDate: LocalDate?, untilTime: LocalTime?
) {
    Column {
        Text(
            text = "$contentType $timeContent",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${fromDate ?: ""}" +
                    " ~ " +
                    "${untilDate ?: ""}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "${fromTime ?: ""}" +
                    "~ " +
                    "${untilTime ?: ""}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ContentTypeTabs(
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(id.toString(), modifier = Modifier.weight(0.1f))
        Text(contentType, modifier = Modifier.weight(0.1f))
        Text(content, modifier = Modifier.weight(0.2f))
        Text(text = fromDateTime.toString(), modifier = Modifier.weight(0.2f))
        Text(text = untilDateTime.toString(), modifier = Modifier.weight(0.2f))
    }
}

// TimeDialogのための色セット
@Composable
private fun getColors(dialogColor: Color): TimePickerColors {
    val colors: TimePickerColors = if (isSystemInDarkTheme()) {
        TimePickerDefaults.colors(
            activeBackgroundColor = dialogColor.copy(0.3f),
            activeTextColor = Color.White,
            selectorColor = dialogColor,
            inactiveBackgroundColor = Color(0xFF292929)
        )
    } else {
        TimePickerDefaults.colors(
            inactiveBackgroundColor = Color.LightGray,
            activeBackgroundColor = dialogColor.copy(0.1f),
            activeTextColor = dialogColor,
            selectorColor = dialogColor
        )
    }
    return colors
}

// Dialogのボタン
@Composable
private fun MaterialDialogButtons.DefaultDateTimeDialogButtons() {
    positiveButton("OK")
    negativeButton("Cancel")
}

