package com.example.myLifeLog.model.db.location

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LocationDao {
    @Insert
    fun insertLocation(location: Location)

    @Update
    fun updateLocation(location: Location)

    @Query("SELECT * FROM location")
    fun getAllLocationsNotLive(): List<Location>

    @Query("SELECT * FROM location")
    fun getAllLocations(): LiveData<List<Location>>

    @Query("DELETE FROM location")
    fun clearTable()
}