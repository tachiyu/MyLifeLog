package com.example.myLifeLog.model.db.location

import kotlinx.coroutines.*

class LocationRepository(private val locationDao: LocationDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertLocation(location: Location) {
        coroutineScope.launch(Dispatchers.IO) {
            locationDao.insertLocation(location)
        }
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