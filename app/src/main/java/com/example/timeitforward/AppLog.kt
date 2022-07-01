package com.example.timeitforward

import android.os.Build
import androidx.annotation.RequiresApi

// アプリ起動時or更新ボタン押下時にアプリのログをとってきてinsertTimeLogでデータベースに保存する。
@RequiresApi(Build.VERSION_CODES.O)
fun loadAppLogs(viewModel: TimeLogViewModel) {

//* TODO
//
//
//    insertTimeLog(
//        contentType = "アプリ",
//        timeContent = "", // アプリの名前
//        fromDateTime = , // 開始時間
//        untilDateTime = ,// 終了時間
//        viewModel = viewModel
//    )
}