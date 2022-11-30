package com.example.timeitforward

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.timeitforward.model.db.MainRoomDatabase
import com.example.timeitforward.model.db.timelog.TimeLog
import com.example.timeitforward.model.db.timelog.TimeLogRepository
import com.example.timeitforward.model.db.transition.Transition
import com.example.timeitforward.model.db.transition.TransitionRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class MainViewModel(application: Application) : ViewModel() {

    val allTimeLogs: LiveData<List<TimeLog>>
    private val timeLogRepository: TimeLogRepository
    val searchResults: MutableLiveData<List<TimeLog>>

    val allTransitions: LiveData<List<Transition>>
    private val transitionRepository: TransitionRepository

    init {
        val db = MainRoomDatabase.getInstance(application)
        val timeLogDao = db.TimeLogDao()
        val transitionDao = db.TransitionDao()
        timeLogRepository = TimeLogRepository(timeLogDao)
        transitionRepository = TransitionRepository(transitionDao)

        allTimeLogs = timeLogRepository.allTimeLogs
        searchResults = timeLogRepository.searchResults
        allTransitions = transitionRepository.allTransition
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

    fun deleteTimeLog(id: Int) {
        timeLogRepository.deleteTimeLog(id)
    }

    fun insertTransition(transition: Transition) {
        transitionRepository.insertTransition(transition)
    }
}


// TimeLogの保存
fun insertTimeLog(
    contentType: String = "", timeContent: String = "",
    fromDateTime: LocalDateTime?, untilDateTime: LocalDateTime?,
    viewModel: MainViewModel
) {
    if (
        (fromDateTime != null) // 開始時刻がnullでない
        && (untilDateTime != null) // 終了時刻がnullでない
        && (untilDateTime > fromDateTime) // 開始時間が終了時間より先
        && (ChronoUnit.MINUTES.between(untilDateTime, fromDateTime) < 24 * 60) // 開始時刻と終了時刻の差が1日以内
    ) {
        // TimeLogをデータベースに挿入
        viewModel.insertTimeLog(
            TimeLog(
                contentType = contentType.ifBlank { "不明" },
                timeContent = timeContent.ifBlank { "不明" },
                fromDateTime = fromDateTime,
                untilDateTime = untilDateTime
            )
        )
    } else {
        // ログで通知
        Log.e("insertTimeLog", "Invalid time record")

    }
}
// TimeLogオブジェクトでできる版
fun insertTimeLog(timeLog: TimeLog, viewModel: MainViewModel) {
    if (
        (timeLog.untilDateTime > timeLog.fromDateTime) // 開始時間が終了時間より先
        && (ChronoUnit.MINUTES.between(timeLog.untilDateTime, timeLog.fromDateTime) < 24 * 60) // 開始時刻と終了時刻の差が1日以内
    ) {
        // TimeLogをデータベースに挿入
        viewModel.insertTimeLog(timeLog)
    } else {
        // ログで通知
        Log.e("insertTimeLog", "Invalid time record")

    }
}

fun insertTransition(transition: Transition, viewModel: MainViewModel) {
    viewModel.insertTransition(transition)
}