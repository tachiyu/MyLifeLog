package com.example.myLifeLog.model.db.transition

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myLifeLog.Transition

@Dao
interface TransitionDao {
    @Insert
    suspend fun insertTransition(transition: Transition)

    @Query("SELECT * FROM transition")
    fun getAllTransitions(): List<Transition>

    @Query("WITH nearest_log AS (SELECT * FROM transition WHERE datetime < :fromDateTime ORDER BY datetime DESC LIMIT 1), " +
            "range_logs AS (SELECT * FROM transition WHERE datetime >= :fromDateTime AND datetime <= :untilDateTime ORDER BY datetime ASC) " +
            "SELECT * FROM nearest_log " +
            "UNION ALL " +
            "SELECT * FROM range_logs;")
    fun getTransitionLogsInRangeWithNearestPrevious(fromDateTime: Long, untilDateTime: Long): List<Transition>
}