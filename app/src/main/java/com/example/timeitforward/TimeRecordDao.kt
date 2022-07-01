package com.example.timeitforward

import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.LocalDateTime

@Dao
interface TimeRecordDao {

    @Insert
    fun insertTimeRecord(timeRecord: TimeRecord)

    @Query("DELETE FROM time_records WHERE time_record_id = :id")
    fun deleteTimeRecord(id: Int)

    @Query("SELECT * FROM time_records WHERE time_content = :content")
    fun findTimeRecordByContent(content: String): List<TimeRecord>

    @Query("SELECT * FROM time_records WHERE NOT ((from_datetime >= :untilDateTime) AND (until_datetime <= :fromDateTime))")
    fun findOverlappingTimeRecords(untilDateTime: LocalDateTime, fromDateTime: LocalDateTime): LiveData<List<TimeRecord>>

    @Update
    fun updateTimeRecord(timeRecord: TimeRecord)

    @Query("SELECT * FROM time_records")
    fun getAllTimeRecords(): LiveData<List<TimeRecord>>
}