package com.example.timeitforward.model.db.sleep

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.time.LocalDateTime

class SleepRepository(private val sleepDao: SleepDao) {

    val allSleeps: LiveData<List<Sleep>> = sleepDao.getAllSleeps()
    val searchResults = MutableLiveData<List<Sleep>>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)


    fun insertSleep(sleep: Sleep) {
        coroutineScope.launch(Dispatchers.IO) {
            sleepDao.insertSleep(sleep)
        }
    }

    fun getSleepBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Sleep> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async sleepDao.getSleepBetween(fromDateTime, untilDateTime)
            }.await()
        }
    }

    fun clearTable() {
        coroutineScope.launch(Dispatchers.IO) {
            sleepDao.clearTable()
        }
    }
}
