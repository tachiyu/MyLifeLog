package com.example.myLifeLog.model.db.timelog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TimeLogDao {

    @Insert
    fun insertTimeLog(timeLog: TimeLog)

    @Query("SELECT * FROM time_log")
    fun getAllTimeLogs(): List<TimeLog>

    @Query("SELECT * FROM time_log " +
            "WHERE content_type = :contentType " +
            "AND :fromDateTime <= until_datetime " +
            "AND :untilDateTime >= from_datetime ")
    fun findTimeLogs(
        contentType: Int,
        fromDateTime: Long,
        untilDateTime: Long ): List<TimeLog>

}