package com.example.myLifeLog.model.db.timelog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.time.LocalDateTime

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

    fun findTimeLogBetweenDateTimes(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime) {
        coroutineScope.launch(Dispatchers.Main) {
            searchResults.value = coroutineScope.async(Dispatchers.IO) {
                return@async timeLogDao.findTimeLogBetweenDateTimes(fromDateTime, untilDateTime)
            }.await()
        }
    }

    fun findTimeLogOfContentTypeBetweenDateTimes(
        fromDateTime: LocalDateTime,
        untilDateTime: LocalDateTime,
        content_type: String
    ) { coroutineScope.launch(Dispatchers.Main) {
            searchResults.value = coroutineScope.async(Dispatchers.IO) {
                return@async timeLogDao.findTimeLogOfContentTypeBetweenDateTimes(
                    fromDateTime, untilDateTime, content_type
                )
            }.await()
        }
    }

    fun getLastLogInContentType(content_type: String): TimeLog? {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async timeLogDao.findLastLogInContentType(content_type).let {
                    if (it.isEmpty()) { null } else { it[0] }
                }
            }.await()
        }
    }

    fun getFirstLogInContentType(content_type: String): TimeLog? {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async timeLogDao.findFirstLogInContentType(content_type).let {
                    if (it.isEmpty()) { null } else { it[0] }
                }
            }.await()
        }
    }

    fun getFirstLog(): TimeLog? {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async timeLogDao.findFirstLog().let {
                    if (it.isEmpty()) { null } else { it[0] }
                }
            }.await()
        }
    }

    fun clearContent(contentType: String) {
        coroutineScope.launch(Dispatchers.IO) {
            timeLogDao.clearContent(contentType)
        }
    }

}
