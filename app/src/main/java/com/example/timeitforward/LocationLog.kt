package com.example.timeitforward

import android.os.Build
import androidx.annotation.RequiresApi

// backgroundで定期的に場所のログを取得して、解析し
// insertTimeLogでデータベースに保存する
@RequiresApi(Build.VERSION_CODES.O)
fun insertLocationTimeLogs(viewModel: TimeLogViewModel) {

//* TODO
//
//
//    insertTimeLog(
//        contentType = "場所",
//        timeContent = "", // 家、会社、電車など…？
//        fromDateTime = , // 開始時間
//        untilDateTime = ,// 終了時間
//        viewModel = viewModel
//    )

}