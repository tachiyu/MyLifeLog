package com.example.timeitforward

import java.time.LocalDateTime

// アプリ起動時or更新ボタン押下時にアプリのログをとってきてinsertTimeLogでデータベースに保存する。

fun loadAppLogs(viewModel: TimeLogViewModel) {

//* TODO
//
//
    insertTimeLog(
        contentType = R.string.app.toString(),
        timeContent = "", // アプリの名前
        fromDateTime = LocalDateTime.MIN, // 開始時間
        untilDateTime = LocalDateTime.MIN,// 終了時間
        viewModel = viewModel
    )
}