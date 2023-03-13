package com.example.myLifeLog.model.db.sleep

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SleepDao {
    @Insert
    fun insertSleep(sleep: Sleep)

    @Query("SELECT * FROM sleep")
    fun getAllSleeps(): List<Sleep>

    @Query("SELECT * FROM sleep " +
            "WHERE :fromDateTime <= datetime AND :untilDateTime >= datetime")
    fun getSleepBetween(fromDateTime: Long, untilDateTime: Long): List<Sleep>
}