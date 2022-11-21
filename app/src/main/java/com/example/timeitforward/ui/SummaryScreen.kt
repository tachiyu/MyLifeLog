package com.example.timeitforward.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.timeitforward.*
import com.example.timeitforward.R
import com.example.timeitforward.model.db.TimeLog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Composable
fun SummaryScreenSetup(viewModel: TimeLogViewModel) {
    val searchResults by viewModel.searchResults.observeAsState(listOf())
    //searchResultsを、contextTypeで、fromDateの00:00からuntilDateの23:59までに動いていたTimeLogのリストに更新する
    fun updateSearchResults(fromDate:LocalDate, untilDate:LocalDate, contentType: String){
        viewModel.findTimeLogOfContentTypeBetweenDateTimes(
            LocalDateTime.of(fromDate, LocalTime.MIN),
            LocalDateTime.of(untilDate, LocalTime.MAX),
            contentType
        )
    }

    SummaryScreen(
        searchResults = searchResults,
        updateSearchResults = ::updateSearchResults,
        firstDate = getFirstDate(viewModel)
    )
}

@Composable
fun SummaryScreen(
    searchResults: List<TimeLog>,
    updateSearchResults: (LocalDate, LocalDate, String) -> Unit,
    firstDate: LocalDate
) {
    var summaryDurationTabIndex by remember { mutableStateOf(2) }
    val contentTabs = listOf(
        stringResource(id = R.string.app),
        stringResource(id = R.string.location),
        stringResource(id = R.string.sleep),
        stringResource(id = R.string.others)
    )
    var contentTabIndex by remember { mutableStateOf(0) }

    val currentDate = LocalDate.now(ZoneId.systemDefault()) //現在の日付

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        when (summaryDurationTabIndex) {
            0 -> MonthPage(Modifier.weight(2f), currentDate, firstDate, searchResults,
                onDateSpanSelected={ fromDate, untilDate -> updateSearchResults(fromDate, untilDate, contentTabs[contentTabIndex]) })
            1 -> WeekPage(Modifier.weight(2f), currentDate, firstDate, searchResults,
                onDateSpanSelected={ fromDate, untilDate -> updateSearchResults(fromDate, untilDate, contentTabs[contentTabIndex]) })
            2 -> DayPage(Modifier.weight(2f), currentDate, firstDate, searchResults,
                onDateSpanSelected={ fromDate, untilDate -> updateSearchResults(fromDate, untilDate, contentTabs[contentTabIndex]) })
        }
        SummaryDurationTabBar(
            modifier = Modifier.height(56.dp),
            selectedTab = summaryDurationTabIndex,
            onTabSwitch = { index, _ -> summaryDurationTabIndex = index }
        )
        TabBar(
            modifier = Modifier,
            tabIndex = contentTabIndex,
            tabData = contentTabs,
            onTabSwitch = {index, text ->
                contentTabIndex = index
            }
        )
    }
}




@Composable
fun MonthPage(modifier: Modifier,
              currentDate: LocalDate,
              firstDate: LocalDate,
              searchResults: List<TimeLog>,
              onDateSpanSelected: (LocalDate, LocalDate) -> Unit
) {
    val dateList = mutableListOf<LocalDate>()
    val currentMonthDay = currentDate.withDayOfMonth(1)
    val firstMonthDay = firstDate.withDayOfMonth(1)
    var tmpMonthDay = currentMonthDay
    while (tmpMonthDay>=firstMonthDay) {
        dateList.add(tmpMonthDay)
        tmpMonthDay = tmpMonthDay.minusMonths(1)
    }
    //PageContent
    var selectedDate by remember { mutableStateOf(dateList[0]) }
    val endDate: LocalDate = selectedDate.plusMonths(1).minusDays(1)
    val timeUnit = TimeUnit.MINUTES.toMillis(1) //分を単位とする
    onDateSpanSelected(selectedDate, endDate)
    Column(modifier = modifier) {
        // 日付選択用のドロップダウンボックス
        DateSelectionDropDown(dateList = dateList, selectedDate = selectedDate) { date ->
            selectedDate = date
            onDateSpanSelected(selectedDate, endDate)
        }

        var appNameSelected: String by remember{ mutableStateOf("") }

        //時間軸に沿ったログの一覧のlazy column
        val timeIndices = mutableListOf<LocalDateTime>()
        var tmpDate = selectedDate
        while (tmpDate <= endDate){
            timeIndices.add(LocalDateTime.of(tmpDate, LocalTime.of(0,0)))
            tmpDate = tmpDate.plusDays(1)
        }
        val timeLine: List<TimeLog> = searchResults
            .toCloseConcatenated(timeUnit)
            .toTimeGapFilled(
                LocalDateTime.of(selectedDate, LocalTime.MIN),
                LocalDateTime.of(endDate, LocalTime.MAX)
            )
            .toTimeIndexInserted(timeIndices)
        LazyColumn(modifier = Modifier.weight(2f)) {
            items(timeLine) { item ->
                TimeLogLine(timeLog = item, timeUnit = timeUnit, heightAlpha = 2f/24f,
                    isPackageNameSelected = appNameSelected == item.timeContent )
            }
        }

        // timeLogSummaryのリスト
        val timeLogSummaryList: List<TimeLogSummary> = searchResults.toTimeLogSummaryList().sorted()
        LazyColumn(modifier = Modifier
            .weight(1f)
            .background(Color(0x446699FF))
        ) {
            items(timeLogSummaryList) { item ->
                TimeLogSummaryRow(
                    timeLogSummary = item,
                    onClick = { appNameSelected = if (appNameSelected != item.timeContent) item.timeContent else "" },
                    non_transparency = if(appNameSelected==item.timeContent) 255 else 128
                )
            }
        }
    }
}

@Composable
fun WeekPage(modifier: Modifier,
             currentDate: LocalDate,
             firstDate: LocalDate,
             searchResults: List<TimeLog>,
             onDateSpanSelected: (LocalDate, LocalDate) -> Unit
) {
    val dateList = mutableListOf<LocalDate>()
    val currentSunday = currentDate.minusDays(currentDate.dayOfWeek.value.toLong())
    val firstSunday = firstDate.minusDays(firstDate.dayOfWeek.value.toLong())
    var tmpSunday = currentSunday
    while (tmpSunday>=firstSunday) {
        dateList.add(tmpSunday)
        tmpSunday = tmpSunday.minusWeeks(1)
    }
    //PageContent
    var selectedDate by remember { mutableStateOf(dateList[0]) }
    val endDate: LocalDate = selectedDate.plusWeeks(1).minusDays(1)
    val timeUnit = TimeUnit.MINUTES.toMillis(1) //分を単位とする
    onDateSpanSelected(selectedDate, endDate)
    Column(modifier = modifier) {
        // 日付選択用のドロップダウンボックス
        DateSelectionDropDown(dateList = dateList, selectedDate = selectedDate) { date ->
            selectedDate = date
            onDateSpanSelected(selectedDate, endDate)
        }

        var appNameSelected: String by remember{ mutableStateOf("") }

        //時間軸に沿ったログの一覧のlazy column
        val timeIndices = mutableListOf<LocalDateTime>()
        var tmpDate = selectedDate
        while (tmpDate <= endDate){
            listOf<Int>(0,6,12,18).forEach {
                timeIndices.add(LocalDateTime.of(tmpDate, LocalTime.of(it,0)))
            }
            tmpDate = tmpDate.plusDays(1)
        }
        val timeLine: List<TimeLog> = searchResults
            .toCloseConcatenated(timeUnit)
            .toTimeGapFilled(
                LocalDateTime.of(selectedDate, LocalTime.MIN),
                LocalDateTime.of(endDate, LocalTime.MAX)
            )
            .toTimeIndexInserted(timeIndices)
        LazyColumn(modifier = Modifier.weight(2f)) {
            items(timeLine) { item ->
                TimeLogLine(timeLog = item, timeUnit = timeUnit, heightAlpha = 2f/6f,
                    isPackageNameSelected = appNameSelected == item.timeContent )
            }
        }

        // timeLogSummaryのリスト
        val timeLogSummaryList: List<TimeLogSummary> = searchResults.toTimeLogSummaryList().sorted()
        LazyColumn(modifier = Modifier
            .weight(1f)
            .background(Color(0x446699FF))
        ) {
            items(timeLogSummaryList) { item ->
                TimeLogSummaryRow(
                    timeLogSummary = item,
                    onClick = { appNameSelected = if (appNameSelected != item.timeContent) item.timeContent else "" },
                    non_transparency = if(appNameSelected==item.timeContent) 255 else 128
                )
            }
        }
    }

}

@Composable
fun DayPage(modifier: Modifier,
            currentDate: LocalDate,
            firstDate: LocalDate,
            searchResults: List<TimeLog>,
            onDateSpanSelected: (LocalDate, LocalDate) -> Unit
) {
    val dateList = mutableListOf<LocalDate>()
    var tmpDate = currentDate
    while (tmpDate>=firstDate) {
        dateList.add(tmpDate)
        tmpDate = tmpDate.minusDays(1)
    }
    //PageContent
    var selectedDate by remember { mutableStateOf(dateList[0]) }
    val timeUnit = TimeUnit.MINUTES.toMillis(1) //分を単位とする
    onDateSpanSelected(selectedDate, selectedDate)
    Column(modifier = modifier) {
        // 日付選択用のドロップダウンボックス
        DateSelectionDropDown(dateList = dateList, selectedDate = selectedDate) { date ->
            selectedDate = date
            onDateSpanSelected(selectedDate, selectedDate)
        }

        var appNameSelected: String by remember{ mutableStateOf("") }

        //時間軸に沿ったログの一覧のlazy column
        val timeLine: List<TimeLog> = searchResults
                        .toCloseConcatenated(timeUnit)
                        .toTimeGapFilled(
                            LocalDateTime.of(selectedDate, LocalTime.MIN),
                            LocalDateTime.of(selectedDate, LocalTime.MAX)
                        )
                        .toTimeIndexInserted(
                            (0..23).toList()
                                .map { LocalDateTime.of(selectedDate, LocalTime.of(it, 0)) })
        LazyColumn(modifier = Modifier.weight(2f)) {
            items(timeLine) { item ->
                TimeLogLine(timeLog = item, timeUnit = timeUnit, heightAlpha = 2f,
                isPackageNameSelected = appNameSelected == item.timeContent )
            }
        }

        // timeLogSummaryのリスト
        val timeLogSummaryList: List<TimeLogSummary> = searchResults.toTimeLogSummaryList().sorted()
        LazyColumn(modifier = Modifier
            .weight(1f)
            .background(Color(0x446699FF))
        ) {
            items(timeLogSummaryList) { item ->
                TimeLogSummaryRow(
                    timeLogSummary = item,
                    onClick = { appNameSelected = if (appNameSelected != item.timeContent) item.timeContent else "" },
                    non_transparency = if(appNameSelected==item.timeContent) 255 else 128
                )
            }
        }
    }
}

// Roomに保存された最古のTimeLogの日付（開始）を出力する。もしTimeLogが1つも存在しなければ現在の日付を出力
private fun getFirstDate(viewModel: TimeLogViewModel): LocalDate {
    return viewModel.getFistLog().let {
        if (it!=null) {
            it.fromDateTime.toLocalDate()
        } else {
            LocalDate.now(ZoneId.systemDefault())
        }
    }
}

//TimeLogを表示するボックス。
@Composable
fun TimeLogLine(timeLog:TimeLog, timeUnit:Long, heightAlpha: Float, modifier: Modifier = Modifier, isPackageNameSelected: Boolean){
    val height = ((timeLog.untilDateTime.toMilliSec() - timeLog.fromDateTime.toMilliSec()) / timeUnit * heightAlpha).toInt().dp
    val context = LocalContext.current
    when (timeLog.contentType) {
        "__IndexedDateTime__" -> Row(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0x88CCFFFF), shape = RoundedCornerShape(4.dp))
            ) {
                Text(text = "${timeLog.fromDateTime.toLocalDate()} ${timeLog.fromDateTime.hour}:00~" )
            }

        "__Dummy__" -> Row(modifier = modifier
            .fillMaxSize()
            .height(height)) {}

        else -> Row(modifier = modifier
            .fillMaxSize()
            .background(
                getAppName(timeLog.timeContent, context).toColor(
                    if (isPackageNameSelected) 255 else 126
                ), shape = RoundedCornerShape(4.dp)
            )
            .height(height)){}
    }
}

@Composable
fun SummaryDurationTabBar(
    modifier: Modifier=Modifier,
    selectedTab: Int,
    onTabSwitch: (Int, String) -> Unit
) {
    val tabs = listOf("月", "週", "日")
    TabBar(modifier = modifier, tabIndex = selectedTab, tabData = tabs, onTabSwitch = onTabSwitch)
}

@Composable
fun DateSelectionDropDown(
    modifier: Modifier = Modifier,
    dateList: List<LocalDate>,
    selectedDate: LocalDate,
    onSelected: (LocalDate) -> Unit,
){
    var dropDownExpanded: Boolean by remember { mutableStateOf(false) }
    Box(modifier = modifier
        .padding(8.dp)
        .background(Color.Gray)) {
        Text(
            selectedDate.toString(),
            modifier = Modifier
                .clickable(onClick = {
                    dropDownExpanded = true
                }))
        DropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { dropDownExpanded = false }) {
            dateList.forEach { item ->
                DropdownMenuItem(onClick = {
                    dropDownExpanded = false
                    onSelected(item)
                }) {
                    Text(text = item.toString())
                }
            }
        }
    }
}

//TimeLogリストに時刻を表すものをはさみこむ。toTimeGapFilledの行われたTimeLogリストでやること。
fun List<TimeLog>.toTimeIndexInserted(indexedDateTimes: List<LocalDateTime>): List<TimeLog> {
    if (this.isEmpty()){ return this }

    val newTimeLogList = mutableListOf<TimeLog>()
    this.forEach { timeLog ->
        var tmpTimeLog = timeLog
        indexedDateTimes.forEach { indexedDateTime ->
            if (tmpTimeLog.fromDateTime <= indexedDateTime && indexedDateTime <= tmpTimeLog.untilDateTime) {
                newTimeLogList.add(TimeLog(
                    contentType = tmpTimeLog.contentType,
                    fromDateTime = tmpTimeLog.fromDateTime,
                    untilDateTime = indexedDateTime,
                    timeContent = tmpTimeLog.timeContent
                ))
                newTimeLogList.add(TimeLog(
                    contentType = "__IndexedDateTime__",
                    fromDateTime = indexedDateTime,
                    untilDateTime = indexedDateTime,
                    timeContent = ""
                ))
                tmpTimeLog = TimeLog(
                    contentType = tmpTimeLog.contentType,
                    fromDateTime = indexedDateTime,
                    untilDateTime = tmpTimeLog.untilDateTime,
                    timeContent = tmpTimeLog.timeContent
                )
            }
        }
        newTimeLogList.add(tmpTimeLog)
    }
    return newTimeLogList
}

fun List<TimeLog>.toTimeGapFilled(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<TimeLog> {
    val newTimeLogList = mutableListOf<TimeLog>()
    var tmpDateTime = fromDateTime

    //TimeLogが空なら、すべてダミーで埋める。
    if(this.isEmpty()) {
        newTimeLogList.add(
            TimeLog(
                contentType = "__Dummy__",
                fromDateTime = fromDateTime,
                untilDateTime = untilDateTime,
                timeContent = "")
        )
        return newTimeLogList
    }
    //TimeLogが空でないなら
    this.forEach {
        //もしtmpDateTimeがit(TimeLog)の開始時間より早ければ、
        if(tmpDateTime < it.fromDateTime) {
            // fromDateTimeがtmpDateTime、untilDateTimeがit.fromDateTimeのダミーTimeLogを新しいリストに追加する
            newTimeLogList.add(
                TimeLog(
                contentType = "__Dummy__",
                fromDateTime = tmpDateTime,
                untilDateTime = it.fromDateTime,
                timeContent = "")
            )
        }
        //itを新しいリストに追加する
        newTimeLogList.add(it)
        //tmpDateTimeにit.untilDateTimeを代入する
        tmpDateTime = it.untilDateTime
    }
    //最後のTimeLogの後ろも埋める
    if(tmpDateTime < untilDateTime) {
        newTimeLogList.add(
            TimeLog(
                contentType = "__Dummy__",
                fromDateTime = tmpDateTime,
                untilDateTime = untilDateTime,
                timeContent = "")
        )
    }
    return newTimeLogList
}

//TimeLogリストの中の異なる要素において、内容が同じで時間間隔が短いもの（thrTimeSpan未満）を連続とみなして接合した、新しいリストを返す。
fun List<TimeLog>.toCloseConcatenated(thrTimeSpan: Long): List<TimeLog> {
    val newTimeLogList = mutableListOf<TimeLog>()

    if (this.isEmpty()){ return this }

    var tmpTimeLog: TimeLog = this[0]
    this.forEach {
        if(tmpTimeLog.timeContent==it.timeContent
            && it.fromDateTime.toMilliSec() - tmpTimeLog.untilDateTime.toMilliSec() < thrTimeSpan) {
            tmpTimeLog = TimeLog(
                contentType = tmpTimeLog.contentType,
                fromDateTime = tmpTimeLog.fromDateTime,
                untilDateTime = it.untilDateTime,
                timeContent = tmpTimeLog.timeContent
            )
        } else {
            newTimeLogList.add(tmpTimeLog)
            tmpTimeLog = it
        }
    }
    newTimeLogList.add(tmpTimeLog)
    return newTimeLogList
}

data class TimeLogSummary(
    val contentType: String,
    val timeContent: String,
    val duration: Long //millisecond
)

//　TimeLogSummaryの情報を表示
@Composable
fun TimeLogSummaryRow(timeLogSummary: TimeLogSummary,
                      onClick: () -> Unit,
                      non_transparency: Int
) { val context: Context = LocalContext.current
    val appName: String = getAppName(timeLogSummary.timeContent, context)
    rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(color = appName.toColor(non_transparency))
            .selectable(false, onClick=onClick)
    ) {
        if (timeLogSummary.contentType == stringResource(id = R.string.app)) {
            AppIcon(appName = timeLogSummary.timeContent, modifier = Modifier.size(40.dp, 40.dp), context = context)
        } else {
            Text(timeLogSummary.contentType, modifier = Modifier.weight(0.1f))
        }

        Text(appName, modifier = Modifier.weight(0.2f))
        Text(text = timeLogSummary.duration.toHMS(), modifier = Modifier.weight(0.2f))
    }
}

fun List<TimeLog>.toTimeLogSummaryList(): List<TimeLogSummary> {
    val contentsSet: Set<Pair<String, String>> = this.map { Pair(it.timeContent, it.contentType) }.toSet()
    val retList = mutableListOf<TimeLogSummary>()
    contentsSet.forEach { pair ->
        val sameTimeLogList = mutableListOf<TimeLog>()
        this.forEach { timeLog ->
            if(timeLog.timeContent == pair.first && timeLog.contentType == pair.second) {
                sameTimeLogList.add(timeLog)
            }
        }
        val duration = sameTimeLogList.sumOf { it.untilDateTime.toMilliSec() - it.fromDateTime.toMilliSec() }
        retList.add(TimeLogSummary(
            timeContent = pair.first, contentType = pair.second, duration = duration
        ))
    }
    return retList
}

fun List<TimeLogSummary>.sorted(): List<TimeLogSummary> {
    val comparator: Comparator<TimeLogSummary> = compareBy<TimeLogSummary> { it.duration }
    return this.sortedWith(comparator).reversed()
}