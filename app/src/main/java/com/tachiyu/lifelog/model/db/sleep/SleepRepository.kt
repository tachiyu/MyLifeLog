package com.tachiyu.lifelog.model.db.sleep

import com.tachiyu.lifelog.Sleep
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

    fun getSleepLogsInRangeWithNearestPrevious(fromDateTime: Long, untilDateTime: Long): List<Sleep> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async sleepDao.getSleepLogsInRangeWithNearestPrevious(fromDateTime, untilDateTime)
            }.await()
        }
    }
}
