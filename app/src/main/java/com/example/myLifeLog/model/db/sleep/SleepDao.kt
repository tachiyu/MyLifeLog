package com.example.myLifeLog.model.db.sleep

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface SleepDao {
    @Insert
    fun insertSleep(sleep: Sleep)

    @Query("SELECT * FROM sleep")
    fun getAllSleeps(): LiveData<List<Sleep>>

    @Query("SELECT * FROM sleep " +
            "WHERE :fromDateTime <= datetime AND :untilDateTime >= datetime")
    fun getSleepBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Sleep>

    @Query("DELETE FROM sleep")
    fun clearTable()
}