package com.example.myLifeLog.model.db.timelog

import com.example.myLifeLog.myLog
import kotlinx.coroutines.*

class TimeLogRepository(private val timeLogDao: TimeLogDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertTimeLog(timeLog: TimeLog) {
        coroutineScope.launch(Dispatchers.IO) {
            myLog("insertTimeLogs", "called!")
            timeLogDao.insertTimeLog(timeLog)
        }
    }

    fun findTimeLogs(contentType: Int, fromDateTime: Long, untilDateTime: Long): List<TimeLog> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                myLog("findTimeLogs", "called!")
                return@async timeLogDao.findTimeLogs(contentType, fromDateTime, untilDateTime)
            }.await()
        }
    }

    fun getAllTimeLogs(): List<TimeLog> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async timeLogDao.getAllTimeLogs()
            }.await()
        }
    }
}
