package com.example.timeitforward

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import java.time.*
import java.util.zip.CRC32

fun getAppName(packageName: String, context: Context): String {
    return try {
        packageManager(context).getApplicationLabel(packageName).toString()
    } catch(e:Exception){
        "$packageName"
    }
}

//StringからCRC32によってハッシュ値をつくり、その値からColorを生成する。non_transparency: 0 ~ 255
fun String.toColor(non_transparency: Int = 255): Color {
    if ((non_transparency in 0..255).not()) {
        error("non_transparency must be in the range 0 to 255")
    }
    val crc = CRC32()
    crc.update(this.hashCode())
    val hash:String
        = Integer.toHexString(non_transparency) + Integer.toHexString(crc.value.toInt()).substring(0..5).uppercase()
    return Color(hash.toUInt(16).toInt())
}

// LocalDate, LocaltimeからLocalDateTimeを作製
fun getLocalDateTime(date: LocalDate?, time: LocalTime?): LocalDateTime? {
    return if ((date != null) && (time != null)) {
        LocalDateTime.of(date, time)
    } else {
        null
    }
}

fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun LocalDateTime.toMilliSec(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

//ミリ秒単位のLongを、”x時間y分z.w秒"に変換して返す。
fun Long.toHMS(): String {
    val hours: Long = this / 1000 / 60 / 60
    val minutes: Long = this / 1000 / 60 % 60
    val seconds: Long = this / 1000 % 60
    val bellowSec: Long = this % 1000 / 100 //秒より下は1桁まで
    return "${hours}時間 ${minutes}分 ${seconds}.${bellowSec}秒"
}

// searchResultsをcontentTypeで絞ってupdateする。
fun updateSearchResultsByContentType(contentType: String, viewModel: MainViewModel, tabData: List<String>) {
    if (contentType == "その他") {
        viewModel.findTimeLogByNotContentTypes(
            tabData.filter { tabText -> tabText != "その他" }
        )
    } else {
        viewModel.findTimeLogByContentType(contentType)
    }
}

// searchResultsをcontentTypeとdateで絞ってupdateする。
fun updateSearchResultsOfContentTypeBetweenDates(
    fromDate: LocalDate,
    untilDate: LocalDate,
    contentType: String,
    viewModel: MainViewModel
) {
    viewModel.findTimeLogOfContentTypeBetweenDateTimes(
        fromDate.atStartOfDay(),
        untilDate.plusDays(1).atStartOfDay().minusNanos(1),
        contentType
    )
}

// For test (現在使用されてない). 各項目に10万件ログを入れてどうなるか
private fun insertBigRecords(viewModel: MainViewModel) {
    val tabData = listOf("場所","睡眠", "アプリ")
    tabData.forEach { tabText ->
        repeat(100000) {
            insertTimeLog(
                contentType = tabText,
                timeContent = "",
                fromDateTime = LocalDateTime.of(2022,1,1,1,1),
                untilDateTime = LocalDateTime.of(2022,1,1,2,1),
                viewModel = viewModel
            )
        }
    }
}
