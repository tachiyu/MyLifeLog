package com.example.timeitforward

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

@RequiresApi(Build.VERSION_CODES.O)
class TimeRecordViewModel(application: Application) : ViewModel() {

    val allTimeRecords: LiveData<List<TimeRecord>>
    private  val repository: TimeRecordRepository
    val searchResults: MutableLiveData<List<TimeRecord>>

    init {
        val timeRecordDb = TimeRecordRoomDatabase.getInstance(application)
        val timeRecordDao = timeRecordDb.timeRecordDao()
        repository = TimeRecordRepository(timeRecordDao)

        allTimeRecords = repository.allTimeRecords
        searchResults = repository.searchResults
    }

    fun insertTimeRecord(timeRecord: TimeRecord) {
        repository.insertTimeRecord(timeRecord)
    }

    fun findTimeRecord(content: String) {
        repository.findTimeRecordByContent(content)
    }

    fun deleteTimeRecord(id: Int) {
        repository.deleteTimeRecord(id)
    }
}