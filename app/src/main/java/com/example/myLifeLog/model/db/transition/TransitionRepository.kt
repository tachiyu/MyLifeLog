package com.example.myLifeLog.model.db.transition

import com.example.myLifeLog.Transition
import kotlinx.coroutines.*

class TransitionRepository(private val transitionDao: TransitionDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    suspend fun insertTransition(transition: Transition) {
        transitionDao.insertTransition(transition)
    }

    fun getAllTransition(): List<Transition> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async transitionDao.getAllTransitions()
            }.await()
        }
    }

    fun getTransitionLogsInRangeWithNearestPrevious(fromDateTime: Long, untilDateTime: Long): List<Transition> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async transitionDao.getTransitionLogsInRangeWithNearestPrevious(fromDateTime, untilDateTime)
            }.await()
        }
    }
}
