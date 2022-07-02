package com.example.timeitforward.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.LocalDateTime

@Dao
interface TimeLogDao {

    @Insert
    fun insertTimeLog(timeLog: TimeLog)

    @Query("DELETE FROM time_logs WHERE time_log_id = :id")
    fun deleteTimeLog(id: Int)

    @Query("SELECT * FROM time_logs WHERE time_content = :content")
    fun findTimeLogByContent(content: String): List<TimeLog>

    @Query("SELECT * FROM time_logs WHERE content_type = :content_type")
    fun findTimeLogByContentType(content_type: String): List<TimeLog>

    @Query("SELECT * FROM time_logs WHERE content_type NOT IN (:content_types)")
    fun findTimeLogByNotContentTypes(content_types: List<String>): List<TimeLog>

    @Query("SELECT * FROM time_logs WHERE NOT ((from_datetime >= :untilDateTime) AND (until_datetime <= :fromDateTime))")
    fun findOverlappingTimeLogs(
        untilDateTime: LocalDateTime,
        fromDateTime: LocalDateTime
    ): LiveData<List<TimeLog>>

    @Update
    fun updateTimeLog(TimeLog: TimeLog)

    @Query("SELECT * FROM time_logs")
    fun getAllTimeLogs(): LiveData<List<TimeLog>>
}