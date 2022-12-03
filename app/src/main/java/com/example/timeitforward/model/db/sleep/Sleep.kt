package com.example.timeitforward.model.db.sleep

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "sleep")
class Sleep(
    @ColumnInfo(name = "confidence") var confidence: Int,
    @ColumnInfo(name = "datetime") var dateTime: LocalDateTime,
    @ColumnInfo(name = "brightness") var brightness: Int,
    @ColumnInfo(name = "motion") var motion: Int
) {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "sleep_id")
    var id: Int = 0
}