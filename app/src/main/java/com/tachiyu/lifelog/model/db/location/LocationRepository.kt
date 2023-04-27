package com.tachiyu.lifelog.model.db.location

import com.tachiyu.lifelog.Location
import kotlinx.coroutines.*

class LocationRepository(private val locationDao: LocationDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    suspend fun insertLocationAndGetId(location: Location): Long {
        return locationDao.insertLocationAndGetId(location)
    }

    fun getAllLocations(): List<Location> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async locationDao.getAllLocations()
            }.await()
        }
    }

    fun getNearLocation(lat: Double, lon: Double): Location? {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async locationDao.getNearLocation(lat, lon)
            }.await()
        }
    }

    fun getLocationById(id: Int): Location? {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async locationDao.getLocationById(id)
            }.await()
        }
    }

    fun updateLocation(location: Location) {
        coroutineScope.launch(Dispatchers.IO) {
            locationDao.updateLocation(location)
        }
    }

}