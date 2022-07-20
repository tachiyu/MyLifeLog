package com.example.timeitforward.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.timeitforward.data.db.TimeLog
import com.example.timeitforward.data.db.TimeLogDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TimeLogRepository(private val timeLogDao: TimeLogDao) {

    val allTimeLogs: LiveData<List<TimeLog>> = timeLogDao.getAllTimeLogs()
    val searchResults = MutableLiveData<List<TimeLog>>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertTimeLog(timeLog: TimeLog) {
        coroutineScope.launch(Dispatchers.IO) {
            timeLogDao.insertTimeLog(timeLog)
        }
    }

    fun deleteTimeLog(id: Int) {
        coroutineScope.launch(Dispatchers.IO) {
            timeLogDao.deleteTimeLog(id)
        }
    }

    fun findTimeLogByContent(content: String) {
        coroutineScope.launch(Dispatchers.Main) {
            searchResults.value = coroutineScope.async(Dispatchers.IO) {
                return@async timeLogDao.findTimeLogByContent(content)
            }.await()
        }
    }

    fun findTimeLogByContentType(contentType: String) {
        coroutineScope.launch(Dispatchers.Main) {
            searchResults.value = coroutineScope.async(Dispatchers.IO) {
                return@async timeLogDao.findTimeLogByContentType(contentType)
            }.await()
        }
    }

    fun findTimeLogByNotContentTypes(contentTypes: List<String>) {
        coroutineScope.launch(Dispatchers.Main) {
            searchResults.value = coroutineScope.async(Dispatchers.IO) {
                return@async timeLogDao.findTimeLogByNotContentTypes(contentTypes)
            }.await()
        }
    }
}
