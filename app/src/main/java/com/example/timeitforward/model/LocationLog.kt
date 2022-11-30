package com.example.timeitforward

import java.time.LocalDateTime

// backgroundで定期的に場所のログを取得して、解析し
// insertTimeLogでデータベースに保存する

fun insertLocationTimeLogs(viewModel: MainViewModel) {
//* TODO
    insertTimeLog(
        contentType = R.string.location.toString(),
        timeContent = "", // 家、会社、電車など…？
        fromDateTime = LocalDateTime.MIN, // 開始時間
        untilDateTime = LocalDateTime.MIN,// 終了時間
        viewModel = viewModel
    )
}