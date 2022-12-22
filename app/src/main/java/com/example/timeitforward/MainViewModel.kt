package com.example.timeitforward

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.timeitforward.model.db.MainRoomDatabase
import com.example.timeitforward.model.db.sleep.Sleep
import com.example.timeitforward.model.db.sleep.SleepRepository
import com.example.timeitforward.model.db.timelog.TimeLog
import com.example.timeitforward.model.db.timelog.TimeLogRepository
import com.example.timeitforward.model.db.transition.Transition
import com.example.timeitforward.model.db.transition.TransitionRepository
import java.time.LocalDateTime

class MainViewModel(application: Application) : ViewModel() {

    val allTimeLogs: LiveData<List<TimeLog>>
    private val timeLogRepository: TimeLogRepository
    val searchResults: MutableLiveData<List<TimeLog>>

    val allTransitions: LiveData<List<Transition>>
    private val transitionRepository: TransitionRepository

    val allSleeps: LiveData<List<Sleep>>
    private  val sleepRepository: SleepRepository

    init {
        val db = MainRoomDatabase.getInstance(application)
        val timeLogDao = db.TimeLogDao()
        val transitionDao = db.TransitionDao()
        val sleepDao = db.SleepDao()
        timeLogRepository = TimeLogRepository(timeLogDao)
        transitionRepository = TransitionRepository(transitionDao)
        sleepRepository = SleepRepository(sleepDao)

        allTimeLogs = timeLogRepository.allTimeLogs
        searchResults = timeLogRepository.searchResults
        allTransitions = transitionRepository.allTransition
        allSleeps = sleepRepository.allSleeps
    }

    fun insertTimeLog(timeLog: TimeLog) {
        timeLogRepository.insertTimeLog(timeLog)
    }

    fun findTimeLogByDateTime(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime) {
        timeLogRepository.findTimeLogBetweenDateTimes(fromDateTime, untilDateTime)
    }

    fun findTimeLogOfContentTypeBetweenDateTimes(fromDateTime: LocalDateTime,
                                                 untilDateTime: LocalDateTime,
                                                 contentType: String) {
        timeLogRepository.findTimeLogOfContentTypeBetweenDateTimes(fromDateTime, untilDateTime, contentType)
    }

    fun findTimeLogByContent(content: String) {
        timeLogRepository.findTimeLogByContent(content)
    }

    fun findTimeLogByContentType(contentType: String) {
        timeLogRepository.findTimeLogByContentType(contentType)
    }

    fun findTimeLogByNotContentTypes(contentTypes: List<String>) {
        timeLogRepository.findTimeLogByNotContentTypes(contentTypes)
    }

    fun getLastLogInContent(contentType: String): TimeLog? {
        return timeLogRepository.getLastLogInContentType(contentType)
    }

    fun getFistLogInContent(contentType: String): TimeLog? {
        return timeLogRepository.getFirstLogInContentType(contentType)
    }

    fun getFistLog(): TimeLog? {
        return timeLogRepository.getFirstLog()
    }

    fun clearContent(contentType: String) {
        timeLogRepository.clearContent(contentType)
    }

    fun deleteTimeLog(id: Int) {
        timeLogRepository.deleteTimeLog(id)
    }

    fun clearTransitionTable() {
        transitionRepository.clearTable()
    }

    fun getTransitionBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Transition> {
        return transitionRepository.getTransitionBetween(fromDateTime, untilDateTime)
    }

    fun clearSleepTable() {
        sleepRepository.clearTable()
    }

    fun getSleepBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Sleep> {
        return sleepRepository.getSleepBetween(fromDateTime, untilDateTime)
    }
}

// TimeLogオブジェクトでできる版
fun insertTimeLog(timeLog: TimeLog, viewModel: MainViewModel) {
    if (
        (timeLog.untilDateTime > timeLog.fromDateTime) // 開始時間が終了時間より先
    ) {
        // TimeLogをデータベースに挿入
        viewModel.insertTimeLog(timeLog)
    } else {
        // ログで通知
        Log.e("insertTimeLog", "Invalid time record")

    }
}