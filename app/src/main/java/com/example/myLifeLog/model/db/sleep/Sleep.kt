package com.example.myLifeLog.model.db.sleep

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sleep", indices = [Index(value = ["datetime"])])
class Sleep(
    @ColumnInfo(name = "confidence") var confidence: Int,
    @ColumnInfo(name = "datetime") var dateTime: Long,
    @ColumnInfo(name = "brightness") var brightness: Int,
    @ColumnInfo(name = "motion") var motion: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sleep_id")
    var id: Int = 0
}