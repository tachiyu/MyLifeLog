package com.example.myLifeLog.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.example.myLifeLog.MainViewModel
import com.example.myLifeLog.R
import com.example.myLifeLog.model.db.timelog.TimeLog
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
fun InputScreenSetup(
    viewModel: MainViewModel,
    navController: NavController,
    periodTabSelected: Int,
    contentTabSelected: Int
) {
    val allTimeLogs by viewModel.allTimeLogs.observeAsState(listOf())

    val navToSummary = {
        navController.navigate("${DESTINATIONS.SUMMARY.str}/$periodTabSelected,$contentTabSelected")
    }

    val backHandlingEnabled by remember { mutableStateOf(true) }
    BackHandler(backHandlingEnabled) {
        navToSummary()
    }
    InputScreen(
        allTimeLogs = allTimeLogs,
        insertTimeLog = { timeLog -> viewModel.insertTimeLog(timeLog) },
        navToSummary = { navToSummary() }
    )
}

@Composable
fun InputScreen(
    allTimeLogs: List<TimeLog>,
    insertTimeLog: (TimeLog) -> Unit,
    navToSummary: () -> Unit
) {
    val dialogColor = remember { Color(0xFF3700B3) }

    // 入力フィールドの値
    val contentType = ContentType.OTHERS
    var timeContent: String by remember { mutableStateOf("") }
    var fromDate: LocalDate? by remember { mutableStateOf(LocalDate.now()) }
    var untilDate: LocalDate? by remember { mutableStateOf(LocalDate.now()) }
    var fromTime: LocalTime? by remember { mutableStateOf(null) }
    var untilTime: LocalTime? by remember { mutableStateOf(null) }

    val timeLogs = allTimeLogs.betweenOf(contentType, LocalDate.MIN, LocalDate.MAX)

    var popupState by remember { mutableStateOf(false) }

    if (popupState) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { popupState = false }
        ) {
            Text(
                modifier = Modifier.background(Color.LightGray),
                text = stringResource(id = R.string.incomplete_input_error)
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            modifier = Modifier.padding(10.dp),
            onClick = navToSummary
        ) {
            Text(modifier = Modifier.padding(10.dp), text = stringResource(id = R.string.back))
        }
        Text(modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.input_others))
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
                if (timeContent != "" && fromDate != null && fromTime != null
                    && untilDate != null && untilTime != null) {
                    insertTimeLog(
                        TimeLog(
                            contentType = contentType,
                            timeContent = timeContent,
                            fromDateTime = LocalDateTime.of(fromDate, fromTime),
                            untilDateTime = LocalDateTime.of(untilDate, untilTime),
                        )
                    )
                    navToSummary()
                } else {
                    popupState = true
                }
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
        placeholder = { Text(stringResource(id = R.string.input_example)) },
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
            text = timeContent,
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

