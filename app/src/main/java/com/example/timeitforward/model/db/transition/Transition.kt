package com.example.timeitforward.model.db.transition

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "transition")
class Transition(
       @ColumnInfo(name = "activity_type") var activityType: Int,
       @ColumnInfo(name = "transition_type") var transitionType: Int,
       @ColumnInfo(name = "datetime") var dateTime: LocalDateTime,
       @ColumnInfo(name = "latitude") var latitude: Double?,
       @ColumnInfo(name = "longitude") var longitude: Double?
) {
       @PrimaryKey(autoGenerate = true)
       @NonNull
       @ColumnInfo(name = "transition_id")
       var id: Int = 0
}