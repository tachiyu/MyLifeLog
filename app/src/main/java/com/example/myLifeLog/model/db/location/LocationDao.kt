package com.example.myLifeLog.model.db.location

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LocationDao {
    @Insert
    fun insertLocation(location: Location)

    @Query("SELECT * FROM location")
    fun getAllLocations(): List<Location>

    @Query("SELECT * FROM location " +
            "WHERE ((:lat-latitude) * (:lat-latitude) + (:lon-longitude) * (:lon-longitude)) * (3.14159265/180*6371*1000) * (3.14159265/180*6371*1000) < 50*50 " +
            "ORDER BY (:lat-latitude) * (:lat-latitude) + (:lon-longitude) * (:lon-longitude) ASC " +
            "LIMIT 1")
    fun getNearLocation(lat: Double, lon: Double): Location?

    @Query("SELECT * FROM location WHERE id = :id LIMIT 1")
    fun getLocationById(id: Int): Location?

    @Update
    fun updateLocation(location: Location)
}