package com.example.myLifeLog.model.db.transition

import com.example.myLifeLog.myLog
import kotlinx.coroutines.*

class TransitionRepository(private val transitionDao: TransitionDao) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertTransition(transition: Transition) {
        coroutineScope.launch(Dispatchers.IO) {
            transitionDao.insertTransition(transition)
        }
    }

    fun getTransitionBetween(fromDateTime: Long, untilDateTime: Long): List<Transition> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async transitionDao.getTransitionBetween(fromDateTime, untilDateTime)
            }.await()
        }
    }

    fun getAllTransition(): List<Transition> {
        return runBlocking {
            myLog("getAllTransition", "called!")
            coroutineScope.async(Dispatchers.IO) {
                return@async transitionDao.getAllTransitions()
            }.await()
        }
    }
}
