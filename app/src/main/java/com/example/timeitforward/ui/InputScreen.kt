package com.example.timeitforward.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.timeitforward.*
import com.example.timeitforward.R
import com.example.timeitforward.model.AppLogManager
import com.example.timeitforward.model.LocationLogManager
import com.example.timeitforward.model.db.timelog.TimeLog
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
fun InputScreenSetup(viewModel: MainViewModel) {
    val allTimeLogs by viewModel.allTimeLogs.observeAsState(listOf())
    val activity = LocalContext.current as MainActivity
    val appLogManager = AppLogManager(activity, viewModel)
    val locationLogManager = LocationLogManager(activity, viewModel)
    val sleepLogManager = SleepLogManager(activity, viewModel)
    InputScreen(
        allTimeLogs = allTimeLogs,
        insertTimeLog = {timeLog -> insertTimeLog(timeLog, viewModel)},
        appLogManager = appLogManager,
        locationLogManager = locationLogManager,
        sleepLogManager = sleepLogManager
    )
}


@Composable
fun InputScreen(
    allTimeLogs: List<TimeLog>,
    insertTimeLog: (TimeLog) -> Unit,
    appLogManager: AppLogManager,
    locationLogManager: LocationLogManager,
    sleepLogManager: SleepLogManager
) {
    val dialogColor = remember { Color(0xFF3700B3) }

    val contentTypes = listOf<String>("app","location","sleep","others")
    var contentTabIndex by remember { mutableStateOf(3) }

    // 入力フィールドの値
    var contentType = "others"
    var timeContent: String by remember { mutableStateOf("") }
    var fromDate: LocalDate? by remember { mutableStateOf(null) }
    var untilDate: LocalDate? by remember { mutableStateOf(null) }
    var fromTime: LocalTime? by remember { mutableStateOf(null) }
    var untilTime: LocalTime? by remember { mutableStateOf(null) }

    val timeLogs = allTimeLogs.betweenOf(contentTypes[contentTabIndex], LocalDate.MIN, LocalDate.MAX)

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        UpdateButton(
            modifier = Modifier,
            appLogManager = appLogManager,
            locationLogManager = locationLogManager,
            sleepLogManager = sleepLogManager
        )
        Text(modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.input_others_logs))
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
                insertTimeLog(
                    TimeLog(
                        contentType = contentType,
                        timeContent = timeContent,
                        fromDateTime = LocalDateTime.of(fromDate, fromTime),
                        untilDateTime = LocalDateTime.of(untilDate, untilTime),
                    )
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
        TimeLogsLazyColumn(timeLogs = timeLogs.reversed(), modifier = Modifier.weight(2f))
        //コンテンツタイプ（アプリ、場所など）を決めるタブ
        AppTabBar(
            modifier = Modifier,
            contentTabIndex = contentTabIndex,
            onTabSwitch = {index, _ -> contentTabIndex = index }
        )
    }
}

@Composable
fun TimeContentField(
    value: String,
    label: String,
    modifier: Modifier,
    readOnly: Boolean = false,
    onValueChange: (String) -> Unit
) {
    TextField(
        value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        readOnly = readOnly
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
    timeContent: String,
    fromDate: LocalDate?, fromTime: LocalTime?,
    untilDate: LocalDate?, untilTime: LocalTime?
) {
    Column {
        Text(
            text = "$timeContent",
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

