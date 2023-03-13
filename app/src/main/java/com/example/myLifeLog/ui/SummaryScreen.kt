package com.example.myLifeLog.ui

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.example.myLifeLog.*
import com.example.myLifeLog.R
import com.example.myLifeLog.model.db.location.Location
import com.example.myLifeLog.model.db.timelog.TimeLog
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

data class Args(
    val getLocationById: (Int) -> Location?,
    val nameLocation: (Location) -> Unit,
    val navToName: (Int, Double, Double) -> Unit
)
val LocalArgs = compositionLocalOf<Args> { error("no args found!") }

@Composable
fun SummaryScreenSetup(
    viewModel: MainViewModel,
    navController: NavController,
    period0: Int = Period.DAY,
    contentType0: Int = ContentType.LOCATION
) {
    val findTimeLogs: (Int, Long, Long) -> List<TimeLog> = { contentType, fromDateTime, untilDateTime ->
        viewModel.findTimeLog(contentType, fromDateTime, untilDateTime)
    }
    var period by remember{ mutableStateOf(period0) }
    var contentType by remember{ mutableStateOf(contentType0) }

    val getLocationById: (Int) -> Location? = { id -> viewModel.getLocationById(id)}
    val updateLocation: (Location) -> Unit = { location -> viewModel.updateLocation(location) }
    val navToName: (Int, Double, Double) -> Unit = { locId, lat, lon ->
        navController.navigate("${DESTINATIONS.NAME}/$locId,$lat,$lon,$period,$contentType")
    }
    val args = Args(
        getLocationById, updateLocation, navToName
    )

    CompositionLocalProvider(LocalArgs provides args) {
        SummaryScreen(
            findTimeLogs = findTimeLogs,
            updateAll = { viewModel.updateAll() },
            navToInput = { navController.navigate("${DESTINATIONS.INPUT.str}/$period,$contentType") },
            navToSetting = { navController.navigate(DESTINATIONS.SETTING.str) },
            period = period,
            onPTabChange = {period2 -> period = period2},
            contentType = contentType,
            onCTabChange = {contentType2 -> contentType = contentType2}
        )
    }
}

@Composable
fun SummaryScreen(
    findTimeLogs: (Int, Long, Long) -> List<TimeLog>,
    updateAll: () -> Unit,
    navToInput: () -> Unit,
    navToSetting: () -> Unit,
    period: Int,
    onPTabChange: (Int) -> Unit,
    contentType: Int,
    onCTabChange: (Int) -> Unit
) {
    // ドロップダウンの最初の日付。最初のAppLogの日付を使う。AppLogがまだ保存されていないとき(0の場合)、
    // ドロップダウンの日付の量が多くなってしまうので、現在の日付にする。
    val firstDate = loadSharedPrefLong(LocalContext.current, "firstAppLogTime").let {
        if (it == 0L) LocalDate.now() else it.toLocalDateTime().toLocalDate()
    }
    val lastDate = LocalDate.now(ZoneId.systemDefault()) //現在の日付
    var cnt by remember { mutableStateOf(0) }

    // SummaryContentをrecomposeさせるためだけの値
    var recomposeVal by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row() {
            Button(
                modifier = Modifier.padding(10.dp),
                onClick = {
                    updateAll()
                    recomposeVal++
                          },
                content = {
                    Image(painter = painterResource(id = R.drawable.reload),
                        modifier = Modifier.size(20.dp, 20.dp),
                        contentDescription = "")
                    Text(text = stringResource(id = R.string.reload))
                }
            )
            if (contentType == ContentType.OTHERS) {
                Button(
                    onClick = navToInput,
                    modifier = Modifier.padding(10.dp),
                    content = {
                        Image(painter = painterResource(id = R.drawable.write),
                            modifier = Modifier.size(20.dp, 20.dp),
                            contentDescription = "")
                        Text(text = stringResource(id = R.string.input_others))
                    }
                )
            }
        }
        //集計期間の単位（日・週・月）を決めるタブ
        PeriodTabBar(
            modifier = Modifier,
            period = period,
            onTabSwitch = { period -> onPTabChange(period)}
        )
        when(period) {
            Period.MONTH -> MonthPage(
                modifier = Modifier.weight(2f),
                lastDate = lastDate,
                firstDate = firstDate,
                findTimeLogs = findTimeLogs,
                contentType = contentType,
                recomposeVal = recomposeVal
            )
            Period.WEEK -> WeekPage(
                modifier = Modifier.weight(2f),
                lastDate = lastDate,
                firstDate = firstDate,
                findTimeLogs = findTimeLogs,
                contentType = contentType,
                recomposeVal = recomposeVal
            )
            Period.DAY -> DayPage(
                modifier = Modifier.weight(2f),
                lastDate = lastDate,
                firstDate = firstDate,
                findTimeLogs = findTimeLogs,
                contentType = contentType,
                recomposeVal = recomposeVal
            )
        }

        //コンテンツタイプ（アプリ、場所など）を決めるタブ
        ContentTypeTabBar(
            modifier = Modifier,
            contentType = contentType,
            onTabSwitch = {
                contentType2 -> onCTabChange(contentType2)
                // 隠しコマンド
                if (contentType2 == ContentType.OTHERS) {
                    cnt++
                    if (cnt == 20) { navToSetting(); cnt = 0 }
                } else {
                    cnt = 0
                }
            }
        )
    }
}

@Composable
fun DayPage(
    modifier: Modifier,
    lastDate: LocalDate,
    firstDate: LocalDate,
    findTimeLogs: (Int, Long, Long) -> List<TimeLog>,
    contentType: Int,
    recomposeVal: Int
) {
    val heightAlpha = 2f

    val startDates = mutableListOf<LocalDate>()
    val endDates = mutableListOf<LocalDate>()
    val datesDisplayed = mutableListOf<String>()

    var tmp = lastDate
    while (tmp >= firstDate) {
        startDates.add(tmp)
        endDates.add(tmp)
        datesDisplayed.add(tmp.toYMDE())
        tmp = tmp.minusDays(1)
    }
    val calcTimeIndices: (LocalDate, LocalDate) -> List<LocalDateTime>
            = { selectedDate, _ ->
        (0..23).toList().map { LocalDateTime.of(selectedDate, LocalTime.of(it, 0)) } }
    SummaryContent(
        modifier = modifier,
        startDates = startDates,
        endDates = endDates,
        datesDisplayed = datesDisplayed,
        heightAlpha = heightAlpha,
        calcTimeIndices = calcTimeIndices,
        findTimeLogs = findTimeLogs,
        contentType = contentType,
        recomposeVal = recomposeVal
    )
}

@Composable
fun WeekPage(
    modifier: Modifier,
    lastDate: LocalDate,
    firstDate: LocalDate,
    findTimeLogs: (Int, Long, Long) -> List<TimeLog>,
    contentType: Int,
    recomposeVal: Int
) {
    val heightAlpha = 2f/6f

    val startDates = mutableListOf<LocalDate>()
    val endDates = mutableListOf<LocalDate>()
    val datesDisplayed = mutableListOf<String>()

    var tmpEnd = lastDate
    var tmpStart = max(firstDate, tmpEnd.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    while (tmpEnd >= firstDate) {
        endDates.add(tmpEnd)
        startDates.add(tmpStart)
        datesDisplayed.add("${tmpStart.toYMDE()} ~ ${tmpEnd.toYMDE()}")
        tmpEnd = tmpStart.minusDays(1)
        tmpStart = max(firstDate, tmpEnd.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    }

    val calcTimeIndices: (LocalDate, LocalDate) -> List<LocalDateTime>
            = { selectedDate, endDate ->
        val timeIndices = mutableListOf<LocalDateTime>()
        var tmpDate = selectedDate
        while (tmpDate <= endDate){
            listOf<Int>(0,6,12,18).forEach {
                timeIndices.add(LocalDateTime.of(tmpDate, LocalTime.of(it,0)))
            }
            tmpDate = tmpDate.plusDays(1)
        }
        timeIndices }

    SummaryContent(
        modifier = modifier,
        startDates = startDates,
        endDates = endDates,
        datesDisplayed = datesDisplayed,
        heightAlpha = heightAlpha,
        calcTimeIndices = calcTimeIndices,
        findTimeLogs = findTimeLogs,
        contentType = contentType,
        recomposeVal = recomposeVal
    )
}

@Composable
fun MonthPage(
    modifier: Modifier,
    lastDate: LocalDate,
    firstDate: LocalDate,
    findTimeLogs: (Int, Long, Long) -> List<TimeLog>,
    contentType: Int,
    recomposeVal: Int
) {
    val heightAlpha = 2f/24f

    val startDates = mutableListOf<LocalDate>()
    val endDates = mutableListOf<LocalDate>()
    val datesDisplayed = mutableListOf<String>()

    var tmpEnd = lastDate
    var tmpStart = max(firstDate, lastDate.withDayOfMonth(1))
    while (tmpEnd >= firstDate) {
        endDates.add(tmpEnd)
        startDates.add(tmpStart)
        datesDisplayed.add("${tmpStart.toYMDE()} ~ ${tmpEnd.toYMDE()}")
        tmpEnd = tmpStart.minusDays(1)
        tmpStart = max(firstDate, tmpEnd.withDayOfMonth(1))
    }
    val calcTimeIndices: (LocalDate, LocalDate) -> List<LocalDateTime>
            = { selectedDate, endDate ->
                val timeIndices = mutableListOf<LocalDateTime>()
                var tmpDate = selectedDate
                while (tmpDate <= endDate){
                    timeIndices.add(LocalDateTime.of(tmpDate, LocalTime.of(0,0)))
                    tmpDate = tmpDate.plusDays(1)
                }
                timeIndices
            }
    SummaryContent(
        modifier = modifier,
        startDates = startDates,
        endDates = endDates,
        datesDisplayed = datesDisplayed,
        heightAlpha = heightAlpha,
        calcTimeIndices = calcTimeIndices,
        findTimeLogs = findTimeLogs,
        contentType = contentType,
        recomposeVal = recomposeVal
    )
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun SummaryContent(modifier: Modifier,
                   startDates: List<LocalDate>,
                   endDates: List<LocalDate>,
                   datesDisplayed: List<String>,
                   heightAlpha: Float,
                   calcTimeIndices: (LocalDate, LocalDate) -> List<LocalDateTime>,
                   findTimeLogs: (Int, Long, Long) -> List<TimeLog>,
                   contentType: Int,
                   recomposeVal: Int
) {
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        modifier = modifier,
        count = startDates.size,
        state = pagerState,
        reverseLayout = true
    ) { page ->

        // 表示する日付
        val startDate = startDates[page]
        val endDate = endDates[page]
        val startDateTime = LocalDateTime.of(startDate, LocalTime.MIN).toMilliSec()
        val endDateTime = LocalDateTime.of(endDate, LocalTime.MAX).toMilliSec()

        // 表示するリストの作成
        val timeLogs = findTimeLogs(contentType, startDateTime, endDateTime).cropped(startDate, endDate)
        val timeIndices = calcTimeIndices(startDate, endDate)
        val timeLine: List<TimeLog>
                = timeLogs
            .sortByDateTime()
            .toCloseConcatenated()
            .dropShortLogs(heightAlpha)
            .toTimeGapFilled(startDate, endDate)
            .toTimeIndexInserted(timeIndices.map { it.toMilliSec() })
        val timeLogSummaryList: List<TimeLogSummary> = timeLogs.toTimeLogSummaryList().sorted()

        // Listのスクロール位置を管理する各種状態
        val timeLogSummaryState = rememberLazyListState()
        var appNameSelected: String by remember{ mutableStateOf("") }

        // コンポーザブル
        Column {
            //日付を選択するドロップダウンリスト
            DateSelectionDropDown(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                dropDownList = datesDisplayed,
                index = page,
                onSelected = { index ->
                    coroutineScope.launch { pagerState.scrollToPage(index) }
                }
            )

            // timeLineのlazyList
            Box(
                modifier = Modifier
                    .border(width = 1.dp, color = Color.Gray)
                    .weight(2f)
            ) {
                LazyColumn(
                    state = LazyListState(firstVisibleItemIndex = timeLine.getTimeIndexNow())
                ) {
                    items(timeLine) { item ->
                        TimeLogLine(timeLog = item, heightAlpha = heightAlpha,
                            isPackageNameSelected = appNameSelected == item.timeContent,
                            onClick = {
                                appNameSelected = item.timeContent
                                coroutineScope.launch {
                                    timeLogSummaryState.scrollToItem(
                                        timeLogSummaryList.indexOfFirst { it.timeContent == item.timeContent }
                                    )
                                }
                            }
                        )
                    }
                }

                ClickableSpace(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(min(page+1, startDates.size-1)) } }
                )

                ClickableSpace(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(max(page-1, 0)) } }
                )
            }


            // timeLogSummaryのlazyList
            Box(
                modifier = Modifier
                    .border(width = 1.dp, color = Color.Gray)
                    .weight(1f, true)
                    .background(Color(0x44FFEBCD))
            ) {
                if (timeLogSummaryList.isNotEmpty()) {
                    LazyColumn(
                        state = timeLogSummaryState
                    ) {
                        items(timeLogSummaryList) { item ->
                            TimeLogSummaryRow(
                                contentType = contentType,
                                timeLogSummary = item,
                                onClick = { appNameSelected = if (appNameSelected != item.timeContent) item.timeContent else "" },
                                non_transparency = if(appNameSelected==item.timeContent) 255 else 128,
                                allTime = endDateTime - startDateTime
                            )
                        }
                    }
                } else {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .align(Alignment.Center),
                        fontSize = 20.sp,
                        text = stringResource(id = R.string.no_log))
                }
            }
        }
    }
}

//TimeLogを表示するボックス。
@Composable
fun TimeLogLine(
    timeLog: TimeLog,
    heightAlpha: Float,
    modifier: Modifier = Modifier,
    isPackageNameSelected: Boolean,
    onClick: () -> Unit
){
    val height = calcHeight(timeLog, heightAlpha)
    val context = LocalContext.current
    when (timeLog.timeContent) {
        "__IndexedDateTime__" -> Row(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0x88CCFFFF), shape = RoundedCornerShape(4.dp))
        ) {
            Text(
                text = "${timeLog.fromDateTime.toLocalDateTime().toLocalDate()} " +
                        "${timeLog.fromDateTime.toLocalDateTime().hour}:00~"
            )
        }

        "__Dummy__" -> Row(
            modifier = modifier
                .fillMaxSize()
                .height(height)
        ) {}

        else -> Row(
            modifier = modifier
                .fillMaxSize()
                .background(
                    getAppName(timeLog.timeContent, context).toColor(
                        if (isPackageNameSelected) 255 else 126
                    )
                )
                .height(height)
                .selectable(false, onClick = onClick)
        ) {}
    }
}

@Composable
fun ClickableSpace(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .width(40.dp)
            .fillMaxHeight()
            .clickable { onClick() }
    ) {

    }
}

@Composable
fun DateSelectionDropDown(
    modifier: Modifier = Modifier,
    dropDownList: List<String>,
    index: Int,
    onSelected: (Int) -> Unit,
){
    var dropDownExpanded: Boolean by remember { mutableStateOf(false) }
    Box(modifier = modifier
        .padding(8.dp)
        .clip(shape = RoundedCornerShape(5.dp))
        .background(Color.LightGray),
        contentAlignment = Alignment.Center
        ) {
        Text(
            "▼${dropDownList[index]}",
            modifier = Modifier
                .clickable(onClick = {
                    dropDownExpanded = true
                }))
        DropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { dropDownExpanded = false }) {
            dropDownList.forEachIndexed { index, item ->
                DropdownMenuItem(onClick = {
                    dropDownExpanded = false
                    onSelected(index)
                }) {
                    Text(text = item)
                }
            }
        }
    }
}

//
fun List<TimeLog>.sortByDateTime(): List<TimeLog> {
    return this.sortedBy { it.fromDateTime  }
}

//TimeLogリストに時刻を表すTimeLog(TimeIndex)を挟んだもを返す。
//toTimeGapFilledの行われたTimeLogリストでやること。
fun List<TimeLog>.toTimeIndexInserted(indexedDateTimes: List<Long>): List<TimeLog> {
    if (this.isEmpty()){ return this }

    val newTimeLogList = mutableListOf<TimeLog>()
    this.forEach { timeLog ->
        var tmpTimeLog = timeLog
        indexedDateTimes.forEach { indexedDateTime ->
            if (tmpTimeLog.fromDateTime <= indexedDateTime && indexedDateTime < tmpTimeLog.untilDateTime) {
                newTimeLogList.add(
                    TimeLog(
                    contentType = tmpTimeLog.contentType,
                    fromDateTime = tmpTimeLog.fromDateTime,
                    untilDateTime = indexedDateTime,
                    timeContent = tmpTimeLog.timeContent
                )
                )
                newTimeLogList.add(
                    TimeLog(
                    contentType = timeLog.contentType,
                    fromDateTime = indexedDateTime,
                    untilDateTime = indexedDateTime,
                    timeContent = "__IndexedDateTime__"
                    )
                )
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

// 現在時刻以下で最大のTimeIndexの位置を取得する。
// 現在時刻を超えるTimeIndexがなかった場合、0.dpを返す。
fun List<TimeLog>.getTimeIndexNow(): Int {
    var timeIndexPos = 0
    var tmp = 0
    val now = LocalDateTime.now().toMilliSec()
    this.forEach { timeLog ->
        if (timeLog.timeContent == "__IndexedDateTime__") {
            if (timeLog.fromDateTime < now) {
                tmp++
                timeIndexPos += tmp
                tmp = 0
            } else {
                return timeIndexPos - 1
            }
        } else {
            tmp++
        }
    }
    return 0
}

fun max(a: LocalDate, b: LocalDate): LocalDate = if (a > b) a else b

fun List<TimeLog>.cropped(fromDate: LocalDate, untilDate: LocalDate)
    = this.map{
        TimeLog(
            it.contentType,
            it.timeContent,
            max(it.fromDateTime, LocalDateTime.of(fromDate, LocalTime.MIN).toMilliSec()),
            min(it.untilDateTime, LocalDateTime.of(untilDate, LocalTime.MAX).toMilliSec())
        )
    }

//TimeLogList内のTimeLog間の時間をダミーLogで埋める（表示のため）
fun List<TimeLog>.toTimeGapFilled(fromDate: LocalDate, untilDate: LocalDate): List<TimeLog> {
    val newTimeLogList = mutableListOf<TimeLog>()
    val fromDateTime = LocalDateTime.of(fromDate, LocalTime.MIN).toMilliSec()
    val untilDateTime = LocalDateTime.of(untilDate, LocalTime.MAX).toMilliSec()
    //TimeLogが空なら、すべてダミーで埋める。
    if(this.isEmpty()) {
        newTimeLogList.add(
            TimeLog(
                contentType = -1,
                fromDateTime = fromDateTime,
                untilDateTime = untilDateTime,
                timeContent = "__Dummy__")
        )
        return newTimeLogList
    }
    //TimeLogが空でないなら
    var tmpDateTime = fromDateTime
    this.forEach {
        //もしtmpDateTimeがit(TimeLog)の開始時間より早ければ、TimeLogの前の時間をダミーTimeLogで埋める
        if(tmpDateTime < it.fromDateTime) {
            // ダミーTimeLogを追加する
            newTimeLogList.add(
                TimeLog(
                contentType = -1 ,
                fromDateTime = tmpDateTime,
                untilDateTime = it.fromDateTime,
                timeContent = "__Dummy__")
            )
        }
        //TimeLogをリストに追加する
        newTimeLogList.add(it)
        //tmpDateTimeにit.untilDateTimeを代入する
        tmpDateTime = it.untilDateTime
    }
    //最後のTimeLogの後ろも埋める
    if(tmpDateTime < untilDateTime) {
        newTimeLogList.add(
            TimeLog(
                contentType = -1,
                fromDateTime = tmpDateTime,
                untilDateTime = untilDateTime,
                timeContent = "__Dummy__")
        )
    }
    return newTimeLogList
}

//TimeLogリストの、内容が同じで間隔が短いもの（thrTimeSpan未満）を連続とみなして結合する。
fun List<TimeLog>.toCloseConcatenated(): List<TimeLog> {
    //元のTimeLogの要素数が1以下ならそのまま返す
    if (this.size <= 1){ return this }

    //間隔5秒以下で同じ内容なら結合
    val thrTimeSpan = TimeUnit.SECONDS.toMillis(5)
    val newTimeLogList = mutableListOf<TimeLog>()
    var tmpTimeLog: TimeLog = this[0]
    this.subList(1,lastIndex+1).forEach { it ->
        tmpTimeLog = if (tmpTimeLog.timeContent == it.timeContent
                && it.fromDateTime - tmpTimeLog.untilDateTime < thrTimeSpan
            ){
                TimeLog(
                    contentType = tmpTimeLog.contentType,
                    fromDateTime = tmpTimeLog.fromDateTime,
                    untilDateTime = it.untilDateTime,
                    timeContent = tmpTimeLog.timeContent
                )
            } else {
                newTimeLogList.add(tmpTimeLog)
                it
            }
    }
    newTimeLogList.add(tmpTimeLog)
    return newTimeLogList
}

data class TimeLogSummary(
    val contentType: Int,
    val timeContent: String,
    val duration: Long //millisecond
)
// TimeLogSummary
@Composable
fun TimeLogSummaryRow(timeLogSummary: TimeLogSummary,
                      onClick: () -> Unit,
                      non_transparency: Int,
                      contentType: Int,
                      allTime: Long
) {
    when(contentType) {
        ContentType.APP -> AppLogSummaryRow(timeLogSummary, onClick, non_transparency, allTime)
        ContentType.LOCATION -> LocationLogSummaryRow(timeLogSummary, onClick, non_transparency, allTime)
        ContentType.SLEEP -> SleepLogSummaryRow(timeLogSummary, onClick, non_transparency, allTime)
        ContentType.OTHERS-> OthersLogSummaryRow(timeLogSummary, onClick, non_transparency, allTime)
    }
}

// TimeLogSummaryの情報を表示（Others用）
@Composable
fun OthersLogSummaryRow(timeLogSummary: TimeLogSummary,
                     onClick: () -> Unit,
                     non_transparency: Int,
                     allTime: Long
) {
    val timeContent = timeLogSummary.timeContent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(color = timeContent.toColor(non_transparency))
            .selectable(false, onClick = onClick)
    ) {
        OthersIcon(modifier = Modifier.size(40.dp, 40.dp))
        Text(timeContent, modifier = Modifier.weight(0.2f))
        Text(text = "${timeLogSummary.duration.toHMS()} ${round(timeLogSummary.duration.toDouble()/allTime.toDouble()*1000.0)/10.0}%", modifier = Modifier.weight(0.3f))
    }
}

//　TimeLogSummaryの情報を表示（App用）
@Composable
fun AppLogSummaryRow(timeLogSummary: TimeLogSummary,
                     onClick: () -> Unit,
                     non_transparency: Int,
                     allTime: Long
) { val context: Context = LocalContext.current
    val appName: String = getAppName(timeLogSummary.timeContent, context)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(color = appName.toColor(non_transparency))
            .selectable(false, onClick = onClick)
    ) {
        AppIcon(appName = timeLogSummary.timeContent, modifier = Modifier.size(40.dp, 40.dp))
        Text(appName, modifier = Modifier.weight(0.2f))
        Text(text = "${timeLogSummary.duration.toHMS()} ${round(timeLogSummary.duration.toDouble()/allTime.toDouble()*1000.0)/10.0}%", modifier = Modifier.weight(0.3f))
    }
}

//　TimeLogSummaryの情報を表示（Location用）
@Composable
fun SleepLogSummaryRow(timeLogSummary: TimeLogSummary,
                       onClick: () -> Unit,
                       non_transparency: Int,
                       allTime: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(color = timeLogSummary.timeContent.toColor(non_transparency))
            .selectable(false, onClick = onClick)
    ) {
        SleepIcon(sleepState = timeLogSummary.timeContent, modifier = Modifier.size(40.dp, 40.dp))
        Text(when(timeLogSummary.timeContent)
            {"sleep" -> stringResource(id = R.string.sleep_state)
                "awake" -> stringResource(id = R.string.awake_state)
                "unsure" -> stringResource(id = R.string.unsure_state)
                else -> ""
            },
            modifier = Modifier.weight(0.2f)
        )
        Text(text = "${timeLogSummary.duration.toHMS()} ${round(timeLogSummary.duration.toDouble()/allTime.toDouble()*1000.0)/10.0}%", modifier = Modifier.weight(0.3f))
    }
}

//　TimeLogSummaryの情報を表示（Location用）
@Composable
fun LocationLogSummaryRow(
    timeLogSummary: TimeLogSummary,
    onClick: () -> Unit,
    non_transparency: Int,
    allTime: Long,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(color = timeLogSummary.timeContent.toColor(non_transparency))
            .selectable(false, onClick = onClick)
    ) {
        val locContent = timeLogSummary.timeContent.toLocContent()
        ActivityIcon(activityType = locContent.activityType, modifier = Modifier.size(40.dp, 40.dp))
        if (locContent.activityType == DetectedActivity.STILL) {
            if (locContent.lat != null && locContent.lon != null) {
                val locName: String? = LocalArgs.current.getLocationById(locContent.locId!!)?.name
                var popupState by remember { mutableStateOf(false) }
                if (locName == null || locName == "") {
                    Button(
                        onClick = { popupState = true },
                        modifier = Modifier
                            .weight(0.2f)
                            .height(40.dp)
                    ) {
                        Text(text = stringResource(id = R.string.show_map))
                    }
                } else {
                    Text(modifier = Modifier
                        .weight(0.2f)
                        .clickable { popupState = true }, text = locName)
                }
                if (popupState){
                    GoogleMapPopup(
                        modifier = Modifier.size(300.dp, 300.dp),
                        locContent = locContent,
                        closeThis = { popupState = false },
                    )
                }
            } else {
                Text(modifier = Modifier.weight(0.2f), text = stringResource(id = R.string.null_location))
            }
        } else { Text(modifier = Modifier
            .weight(0.2f)
            .fillMaxWidth(), text = "")
        }
        Text(text = "${timeLogSummary.duration.toHMS()} ${round(timeLogSummary.duration.toDouble()/allTime.toDouble()*1000.0)/10.0}%", modifier = Modifier.weight(0.3f))
    }
}

@Composable
fun GoogleMapPopup(modifier: Modifier, locContent: LocContent, closeThis: () -> Unit) {
    val loc = LatLng(locContent.lat!!, locContent.lon!!)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(loc, 15f)
    }
    val navToName = LocalArgs.current.navToName
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = closeThis
    ) {
        Column {
            Button(onClick={ navToName(
                locContent.locId!!,
                locContent.lat,
                locContent.lon
            ) }, content = { Text(text = stringResource(id = R.string.name_location))} )
            GoogleMap(
                modifier = modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(state = MarkerState(position = loc))
            }
        }
    }
}

fun List<TimeLog>.toTimeLogSummaryList(): List<TimeLogSummary> {
    val contentsSet: Set<Pair<String, Int>> = this.map { Pair(it.timeContent, it.contentType) }.toSet()
    val retList = mutableListOf<TimeLogSummary>()
    contentsSet.forEach { pair ->
        val sameTimeLogList = mutableListOf<TimeLog>()
        this.forEach { timeLog ->
            if(timeLog.timeContent == pair.first && timeLog.contentType == pair.second) {
                sameTimeLogList.add(timeLog)
            }
        }
        val duration = sameTimeLogList.sumOf { it.untilDateTime - it.fromDateTime }
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

fun List<TimeLog>.dropShortLogs(heightAlpha: Float): List<TimeLog> {
    return this.filter { timeLog -> calcHeight(timeLog, heightAlpha) > 1.dp }
}

fun calcHeight(timeLog: TimeLog, heightAlpha: Float): Dp
    = ((timeLog.untilDateTime - timeLog.fromDateTime).toDouble() / TimeUnit.MINUTES.toMillis(1) * heightAlpha).dp
