package com.example.myLifeLog.model.db.sleep

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myLifeLog.Sleep

@Dao
interface SleepDao {
    @Insert
    fun insertSleep(sleep: Sleep)

    @Query("SELECT * FROM sleep")
    fun getAllSleeps(): List<Sleep>

    @Query("SELECT * FROM sleep " +
            "WHERE :fromDateTime <= datetime AND :untilDateTime >= datetime")
    fun getSleepBetween(fromDateTime: Long, untilDateTime: Long): List<Sleep>

    @Query("WITH nearest_log AS (SELECT * FROM sleep WHERE datetime < :fromDateTime ORDER BY datetime DESC LIMIT 1), " +
            "range_logs AS (SELECT * FROM sleep WHERE datetime >= :fromDateTime AND datetime <= :untilDateTime ORDER BY datetime ASC) " +
            "SELECT * FROM nearest_log " +
            "UNION ALL " +
            "SELECT * FROM range_logs;")
    fun getSleepLogsInRangeWithNearestPrevious(fromDateTime: Long, untilDateTime: Long): List<Sleep>
}