package com.example.timeitforward.model.db.transition

import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import java.time.LocalDateTime

class TransitionRepository(private val transitionDao: TransitionDao) {

    val allTransition: LiveData<List<Transition>> = transitionDao.getAllTransitions()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertTransition(transition: Transition) {
        coroutineScope.launch(Dispatchers.IO) {
            transitionDao.insertTransition(transition)
        }
    }

    fun getTransitionBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Transition> {
        return runBlocking {
            coroutineScope.async(Dispatchers.IO) {
                return@async transitionDao.getTransitionBetween(fromDateTime, untilDateTime)
            }.await()
        }
    }
}
