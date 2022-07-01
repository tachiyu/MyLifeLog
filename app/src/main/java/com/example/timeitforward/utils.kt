package com.example.timeitforward

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// LocalDate, LocaltimeからLocalDateTimeを作製
@RequiresApi(Build.VERSION_CODES.O)
fun getLocalDateTime(date: LocalDate?, time: LocalTime?): LocalDateTime? {
    return if ((date != null) && (time != null)) {
        LocalDateTime.of(date, time)
    } else {
        null
    }
}

// For test (現在使用されてない). 各項目に10万件ログを入れてどうなるか
@RequiresApi(Build.VERSION_CODES.O)
private fun insertBigRecords(viewModel: TimeLogViewModel) {
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