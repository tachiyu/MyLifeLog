package com.example.timeitforward

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.timeitforward.db.TimeLog
import com.example.timeitforward.db.TimeLogDao
import kotlinx.coroutines.*

class TimeLogRepository (private val timeLogDao: TimeLogDao) {

    val allTimeLogs: LiveData<List<TimeLog>> = timeLogDao.getAllTimeLogs()
    val searchResults = MutableLiveData<List<TimeLog>>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertTimeLog(timeLog: TimeLog) {
        coroutineScope.launch (Dispatchers.IO) {
            timeLogDao.insertTimeLog(timeLog)
        }
    }

    fun deleteTimeLog(id: Int) {
        coroutineScope.launch (Dispatchers.IO) {
            timeLogDao.deleteTimeLog(id)
        }
    }

    fun findTimeLogByContent(content: String) {
        coroutineScope.launch (Dispatchers.Main) {
            searchResults.value = coroutineScope.async (Dispatchers.IO) {
                return@async timeLogDao.findTimeLogByContent(content)
            }.await()
        }
    }

    fun findTimeLogByContentType(contentType: String) {
        coroutineScope.launch (Dispatchers.Main) {
            searchResults.value = coroutineScope.async (Dispatchers.IO) {
                return@async timeLogDao.findTimeLogByContentType(contentType)
            }.await()
        }
    }

    fun findTimeLogByNotContentTypes(contentTypes: List<String>) {
        coroutineScope.launch (Dispatchers.Main) {
            searchResults.value = coroutineScope.async (Dispatchers.IO) {
                return@async timeLogDao.findTimeLogByNotContentTypes(contentTypes)
            }.await()
        }
    }
}