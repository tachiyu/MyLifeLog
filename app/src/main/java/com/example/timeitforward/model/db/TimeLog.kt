package com.example.timeitforward.model.db

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity(tableName="time_logs")
class TimeLog
    (
    @ColumnInfo(name = "content_type") var contentType: String,
    @ColumnInfo(name = "time_content") var timeContent: String,
    @ColumnInfo(name = "from_datetime") var fromDateTime: LocalDateTime,
    @ColumnInfo(name = "until_datetime") var untilDateTime: LocalDateTime
) {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "time_log_id")
    var id: Int = 0

}