package com.tachiyu.lifelog.model.db.others

import com.tachiyu.lifelog.Others
import kotlinx.coroutines.*

class OthersRepository(private val othersDao: OthersDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertOthers(others: Others) {
        coroutineScope.launch(Dispatchers.IO) {
            othersDao.insertOthers(others)
        }
    }

    fun getOthersLogsInRange(fromDateTime: Long, untilDateTime: Long): List<Others> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async othersDao.getOthersLogsInRange(fromDateTime, untilDateTime)
            }.await()
        }
    }
}
