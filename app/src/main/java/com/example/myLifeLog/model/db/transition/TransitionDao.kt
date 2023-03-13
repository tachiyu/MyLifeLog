package com.example.myLifeLog.model.db.transition

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransitionDao {
    @Insert
    fun insertTransition(transition: Transition)

    @Query("SELECT * FROM transition")
    fun getAllTransitions(): List<Transition>

    @Query("SELECT * FROM transition " +
            "WHERE :fromDateTime <= datetime AND :untilDateTime >= datetime")
    fun getTransitionBetween(fromDateTime: Long, untilDateTime: Long): List<Transition>
}