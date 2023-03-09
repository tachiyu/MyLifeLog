package com.example.myLifeLog.model.db.location

import androidx.lifecycle.LiveData
import kotlinx.coroutines.*

class LocationRepository(private val locationDao: LocationDao) {

    val allLocations: LiveData<List<Location>> = locationDao.getAllLocations()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertLocation(location: Location) {
        coroutineScope.launch(Dispatchers.IO) {
            locationDao.insertLocation(location)
        }
    }

    fun updateLocation(location: Location) {
        coroutineScope.launch(Dispatchers.IO) {
            locationDao.updateLocation(location)
        }
    }

    fun getNearLocation(lat: Double, lon: Double): Location? {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async locationDao.getNearLocation(lat, lon)
            }.await()
        }
    }

    fun clearTable() {
        coroutineScope.launch(Dispatchers.IO) {
            locationDao.clearTable()
        }
    }
}