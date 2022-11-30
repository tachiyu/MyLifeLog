package com.example.timeitforward.model.db.transition

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransitionRepository(private val transitionDao: TransitionDao) {

    val allTransition: LiveData<List<Transition>> = transitionDao.getAllTransitions()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun insertTransition(transition: Transition) {
        coroutineScope.launch(Dispatchers.IO) {
            transitionDao.insertTransition(transition)
        }
    }
}
