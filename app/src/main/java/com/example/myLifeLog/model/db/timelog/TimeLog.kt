package com.example.myLifeLog.model.db.timelog

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName="time_log", indices = [Index(value = ["content_type","from_datetime", "until_datetime"])])
class TimeLog
    (
    @ColumnInfo(name = "content_type") var contentType: Int,
    @ColumnInfo(name = "time_content") var timeContent: String,
    @ColumnInfo(name = "from_datetime") var fromDateTime: Long,
    @ColumnInfo(name = "until_datetime") var untilDateTime: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "time_log_id")
    var id: Int = 0
}