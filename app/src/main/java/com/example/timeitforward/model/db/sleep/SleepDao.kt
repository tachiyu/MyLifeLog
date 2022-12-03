package com.example.timeitforward.model.db.sleep

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SleepDao {
    @Insert
    fun insertSleep(sleep: Sleep)

    @Query("SELECT * FROM sleep")
    fun getAllSleeps(): LiveData<List<Sleep>>
}