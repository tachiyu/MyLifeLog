package com.example.timeitforward.model.db.sleep

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SleepRepository(private val sleepDao: SleepDao) {

    val allSleeps: LiveData<List<Sleep>> = sleepDao.getAllSleeps()
    val searchResults = MutableLiveData<List<Sleep>>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)


    fun insertSleep(sleep: Sleep) {
        coroutineScope.launch(Dispatchers.IO) {
            sleepDao.insertSleep(sleep)
        }
    }
}
