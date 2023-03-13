package com.example.myLifeLog.model.db.sleep

import kotlinx.coroutines.*

class SleepRepository(private val sleepDao: SleepDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertSleep(sleep: Sleep) {
        coroutineScope.launch(Dispatchers.IO) {
            sleepDao.insertSleep(sleep)
        }
    }

    fun getAllSleeps(): List<Sleep> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async sleepDao.getAllSleeps()
            }.await()
        }
    }

    fun getSleepBetween(fromDateTime: Long, untilDateTime: Long): List<Sleep> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async sleepDao.getSleepBetween(fromDateTime, untilDateTime)
            }.await()
        }
    }
}
