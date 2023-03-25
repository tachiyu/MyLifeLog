package com.example.myLifeLog.model.db.others

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myLifeLog.Others

@Dao
interface OthersDao {
    @Insert
    fun insertOthers(others: Others)

    @Query("SELECT * FROM others " +
            "WHERE (until_datetime > :fromDateTime) OR (from_datetime < :untilDateTime)")
    fun getOthersLogsInRange(fromDateTime: Long, untilDateTime: Long): List<Others>
}