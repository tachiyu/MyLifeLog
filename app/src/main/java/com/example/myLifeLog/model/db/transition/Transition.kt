package com.example.myLifeLog.model.db.transition

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transition")
class Transition(
       @ColumnInfo(name = "activity_type") var activityType: Int,
       @ColumnInfo(name = "transition_type") var transitionType: Int,
       @ColumnInfo(name = "datetime") var dateTime: Long,
       @ColumnInfo(name = "latitude") var latitude: Double?,
       @ColumnInfo(name = "longitude") var longitude: Double?
) {
       @PrimaryKey(autoGenerate = true)
       @ColumnInfo(name = "transition_id")
       var id: Int = 0
}