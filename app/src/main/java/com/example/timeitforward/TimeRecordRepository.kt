package com.example.timeitforward

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class TimeRecordRepository (private val timeRecordDao: TimeRecordDao) {

    val allTimeRecords: LiveData<List<TimeRecord>> = timeRecordDao.getAllTimeRecords()
    val searchResults = MutableLiveData<List<TimeRecord>>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertTimeRecord(timeRecord: TimeRecord) {
        coroutineScope.launch (Dispatchers.IO) {
            timeRecordDao.insertTimeRecord(timeRecord)
        }
    }

    fun deleteTimeRecord(id: Int) {
        coroutineScope.launch (Dispatchers.IO) {
            timeRecordDao.deleteTimeRecord(id)
        }
    }

    fun findTimeRecordByContent(name: String) {
        coroutineScope.launch (Dispatchers.Main) {
            searchResults.value = asyncFind(name).await()
        }
    }

    private fun asyncFind(content: String): Deferred<List<TimeRecord>?> =
        coroutineScope.async (Dispatchers.IO) {
            return@async timeRecordDao.findTimeRecordByContent(content)
        }
}