package com.example.myLifeLog.model.db.timelog

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDateTime

@Dao
interface TimeLogDao {

    @Insert
    fun insertTimeLog(timeLog: TimeLog)

    @Query("DELETE FROM time_log WHERE time_log_id = :id")
    fun deleteTimeLog(id: Int)

    @Query("SELECT * FROM time_log WHERE time_content = :content")
    fun findTimeLogByContent(content: String): List<TimeLog>

    @Query("SELECT * FROM time_log WHERE content_type = :content_type")
    fun findTimeLogByContentType(content_type: Int): List<TimeLog>

    @Query("SELECT * FROM time_log WHERE content_type NOT IN (:content_types)")
    fun findTimeLogByNotContentTypes(content_types: List<Int>): List<TimeLog>

    @Query("SELECT * FROM time_log WHERE NOT ((from_datetime >= :untilDateTime) AND (until_datetime <= :fromDateTime))")
    fun findOverlappingTimeLogs(
        untilDateTime: LocalDateTime,
        fromDateTime: LocalDateTime
    ): List<TimeLog>

    @Update
    fun updateTimeLog(TimeLog: TimeLog)

    @Query("SELECT * FROM time_log")
    fun getAllTimeLogs(): LiveData<List<TimeLog>>

    @Query("SELECT * FROM time_log " +
           "WHERE :fromDateTime <= until_datetime " +
            "AND :untilDateTime >= from_datetime")
    fun findTimeLogBetweenDateTimes(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime)
    : List<TimeLog>

    @Query("SELECT * FROM time_log " +
            "WHERE :fromDateTime <= until_datetime " +
            "AND :untilDateTime >= from_datetime " +
            "AND :contentType == content_type")
    fun findTimeLogOfContentTypeBetweenDateTimes(
        fromDateTime: LocalDateTime,
        untilDateTime: LocalDateTime,
        contentType: Int
    ): List<TimeLog>

    @Query( "SELECT * FROM time_log " +
            "WHERE content_type = :contentType " +
            "ORDER BY time_log_id DESC " +
            "LIMIT 1")
    fun findLastLogInContentType(contentType: Int): List<TimeLog>

    @Query( "SELECT * FROM time_log " +
            "WHERE content_type = :contentType " +
            "LIMIT 1")
    fun findFirstLogInContentType(contentType: Int): List<TimeLog>

    @Query( "SELECT * FROM time_log " +
            "LIMIT 1")
    fun findFirstLog(): List<TimeLog>

    @Query("DELETE FROM time_log WHERE content_type = :contentType")
    fun clearContent(contentType: Int)

}