package com.example.myLifeLog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// タイムログクラス
data class TimeLog(
    val timeContent: String,
    val fromDateTime: Long,
    val untilDateTime: Long
)

// アプリケーションログクラス
data class App(
    val packageName: String,
    val timeStamp: Long,
    val eventType: Int
)

// ロケーションログクラス
@Entity(tableName = "location")
class Location(
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
}

// スリープログクラス
@Entity(tableName = "sleep", indices = [Index(value = ["datetime"])])
class Sleep(
    @ColumnInfo(name = "confidence") var confidence: Int,
    @ColumnInfo(name = "datetime") var dateTime: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
}

// トランジションログクラス
@Entity(tableName = "transition", indices = [Index(value = ["datetime"])])
class Transition(
    @ColumnInfo(name = "activity_type") var activityType: Int,
    @ColumnInfo(name = "transition_type") var transitionType: Int,
    @ColumnInfo(name = "location_id") var locationId: Int?,
    @ColumnInfo(name = "datetime") var dateTime: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
}

// アザーズログクラス
@Entity(tableName = "others")
class Others(
    @ColumnInfo(name = "time_content") var timeContent: String,
    @ColumnInfo(name = "from_datetime") var fromDateTime: Long,
    @ColumnInfo(name = "until_datetime") var untilDateTime: Long

) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
}