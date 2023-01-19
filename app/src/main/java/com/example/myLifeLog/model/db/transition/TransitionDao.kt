package com.example.myLifeLog.model.db.transition

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface TransitionDao {
    @Insert
    fun insertTransition(transition: Transition)

    @Query("SELECT * FROM transition")
    fun getAllTransitions(): LiveData<List<Transition>>

    @Query("SELECT * FROM transition " +
            "WHERE :fromDateTime <= datetime AND :untilDateTime >= datetime")
    fun getTransitionBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Transition>

    @Query("DELETE FROM transition")
    fun clearTable()
}