package com.example.timeitforward

import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Entity(tableName="time_records")
class TimeRecord
    (
    @ColumnInfo(name = "content_type") var contentType: String,
    @ColumnInfo(name = "time_content") var timeContent: String,
    @ColumnInfo(name = "from_datetime") var fromDateTime: LocalDateTime,
    @ColumnInfo(name = "until_datetime") var untilDateTime: LocalDateTime
) {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "time_record_id")
    var id: Int = 0

}