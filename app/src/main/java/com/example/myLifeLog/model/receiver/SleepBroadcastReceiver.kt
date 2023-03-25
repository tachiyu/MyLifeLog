package com.example.myLifeLog.model.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myLifeLog.Sleep
import com.example.myLifeLog.model.db.MainRoomDatabase
import com.example.myLifeLog.model.db.sleep.SleepRepository
import com.example.myLifeLog.myLog
import com.google.android.gms.location.SleepClassifyEvent

// タグ名定義
private const val TAG = "SleepBroadcastReceiver"

// SleepBroadcastReceiverクラス
class SleepBroadcastReceiver : BroadcastReceiver() {

    // onReceiveメソッド：ブロードキャストが受信されたときに呼び出される
    override fun onReceive(context: Context, intent: Intent) {
        // インテントから睡眠データが含まれているか確認
        if (SleepClassifyEvent.hasEvents(intent)) {
            // インテントから睡眠データを抽出
            val events = SleepClassifyEvent.extractEvents(intent)
            // 受信したイベントの数をログに記録
            myLog(TAG, "receive ${events.size} sleep events", context)

            // データベースインスタンスの取得
            val dB: MainRoomDatabase = MainRoomDatabase.getInstance(context)
            // 睡眠リポジトリの作成
            val sleepRepository = SleepRepository(dB.SleepDao())

            // 各イベントについて、データベースに挿入
            for (event in events) {
                sleepRepository.insertSleep(
                    Sleep(
                        confidence = event.confidence,
                        dateTime = event.timestampMillis,
                    )
                )
            }
        }
    }

    // 伴うオブジェクト
    companion object {
        // アクション定数
        const val ACTION_PROCESS_UPDATES =
            "com.example.myLifeLog.action.SLEEP_UPDATES"
    }
}
