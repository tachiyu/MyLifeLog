package com.example.myLifeLog

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.myLifeLog.model.db.MainRoomDatabase
import com.example.myLifeLog.model.db.location.Location
import com.example.myLifeLog.model.db.location.LocationRepository
import com.example.myLifeLog.model.db.sleep.Sleep
import com.example.myLifeLog.model.db.sleep.SleepRepository
import com.example.myLifeLog.model.db.timelog.TimeLog
import com.example.myLifeLog.model.db.timelog.TimeLogRepository
import com.example.myLifeLog.model.db.transition.Transition
import com.example.myLifeLog.model.db.transition.TransitionRepository
import com.example.myLifeLog.ui.ContentType
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import java.time.LocalDateTime

class MainViewModel(private val application: Application) : ViewModel() {

    val allTimeLogs: LiveData<List<TimeLog>>
    private val timeLogRepository: TimeLogRepository

    val allLocations: LiveData<List<Location>>
    private val locationRepository: LocationRepository

    val allTransitions: LiveData<List<Transition>>
    private val transitionRepository: TransitionRepository

    val allSleeps: LiveData<List<Sleep>>
    private  val sleepRepository: SleepRepository

    init {
        val db = MainRoomDatabase.getInstance(application)
        val timeLogDao = db.TimeLogDao()
        val locationDao = db.LocationDao()
        val transitionDao = db.TransitionDao()
        val sleepDao = db.SleepDao()
        timeLogRepository = TimeLogRepository(timeLogDao)
        locationRepository = LocationRepository(locationDao)
        transitionRepository = TransitionRepository(transitionDao)
        sleepRepository = SleepRepository(sleepDao)

        allTimeLogs = timeLogRepository.allTimeLogs
        allLocations = locationRepository.allLocations
        allTransitions = transitionRepository.allTransition
        allSleeps = sleepRepository.allSleeps
    }

    fun insertTimeLog(timeLog: TimeLog) {
        if (timeLog.untilDateTime > timeLog.fromDateTime /*開始時間が終了時間より先*/) {
            timeLogRepository.insertTimeLog(timeLog)
        } else {
            // ログで通知
            Log.e("insertTimeLog", "Invalid time record:" +
                    " ${timeLog.contentType}" +
                    " ${timeLog.timeContent}" +
                    " ${timeLog.fromDateTime}" +
                    " ${timeLog.untilDateTime}"
            )
        }
    }

    private fun insertLocation(location: Location) {
        locationRepository.insertLocation(location)
    }

    fun updateLocation(location: Location) {
        locationRepository.updateLocation(location)
    }

    private fun getTransitionBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Transition> {
        return transitionRepository.getTransitionBetween(fromDateTime, untilDateTime)
    }

    private fun getSleepBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Sleep> {
        return sleepRepository.getSleepBetween(fromDateTime, untilDateTime)
    }

    private fun getNearLocation(lat: Double, lon: Double): Location? {
        return locationRepository.getNearLocation(lat, lon)
    }

    // 最後に保存されたAppログから最新までのAppログを取得しRoomに保存する
    fun updateAppLogs() {
        val tag = "updateAppLogs"
        val contentType = ContentType.APP
        myLog(tag, "updateAppLogs called")
        val usageStatsManager: UsageStatsManager =
            application.applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val lastAppUpdatedTime = loadSharedPrefLong(application.applicationContext, "lastAppUpdatedTime")
        val currentTime = System.currentTimeMillis()
        val usageEvents: UsageEvents = usageStatsManager.queryEvents(
            lastAppUpdatedTime,
            currentTime
        )

        var timeContent = ""
        var fromDateTime: LocalDateTime = LocalDateTime.MIN //初期値は使わないがコンパイルエラー回避
        var untilDateTime: LocalDateTime

        var isForeground = false
        var isFirst = true
        while (usageEvents.hasNextEvent()) {
            val event: UsageEvents.Event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            // もしAppLogがupdateされたのが初めて（lastAppUpdatedTime==0）なら
            // 最初のAppLogの日付をfirstDateとして保存しておく（日付のドロップダウンリストなどに使用する)
            if (isFirst && lastAppUpdatedTime == 0L) {
                saveSharedPref(application.applicationContext, "firstAppLogTime", event.timeStamp)
                isFirst = false
            }

            if ("launcher" !in event.packageName) {
                if (event.eventType== UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (isForeground) {
                        if (timeContent != event.packageName) {
                            Log.e(tag, "timeContent miss match $timeContent, ${event.packageName}")
                        }
                    } else {
                        timeContent = event.packageName
                        fromDateTime = event.timeStamp.toLocalDateTime()
                    }
                    isForeground = true
                } else if (event.eventType== UsageEvents.Event.ACTIVITY_PAUSED){
                    if (isForeground) {
                        if (timeContent != event.packageName) {
                            Log.e(tag, "timeContent miss match $timeContent, ${event.packageName}")
                        }
                        untilDateTime = event.timeStamp.toLocalDateTime()
                        insertTimeLog(
                            TimeLog(
                                contentType = contentType,
                                timeContent = timeContent,
                                fromDateTime = fromDateTime, // 開始時間
                                untilDateTime = untilDateTime,// 終了時間
                            )
                        )
                    } else {
                        Log.e(tag, "double background ${event.packageName} ${event.timeStamp.toLocalDateTime()}")
                    }
                    isForeground = false
                }
            }
        }

        saveSharedPref(application.applicationContext, "lastAppUpdatedTime", currentTime)
    }

    fun insertLocationTimeLog(
        acTyp: Int, locId0: Int?, lat0: Double?, lon0: Double?, fromDateTime: LocalDateTime, untilDateTime: LocalDateTime
    ) {
        val (locId, lat, lon) =
                if (lat0 != null && lon0 != null) {
                    getOrSetNearLocation(lat0, lon0).let {
                        listOf(it.id, it.latitude, it.longitude)
                    }
                } else {
                    listOf(locId0, lat0, lon0)
                }
        insertTimeLog(
            TimeLog(
            ContentType.LOCATION,
            "$acTyp,$locId,$lat,$lon",
                fromDateTime,
                untilDateTime
            )
        )
    }

    fun updateLocationLogs(){
        val tag = "updateLocationLogs"
        val context = application.applicationContext
        myLog(tag, "updateLocationLogs called")

        if (loadSharedPrefBool(context, "IsActivityRecognitionSubscribed")) {
            val lastUpdateTime: LocalDateTime
                    = loadSharedPrefLong(context, "lastLocationUpdatedTime").toLocalDateTime()
            if (lastUpdateTime == 0L.toLocalDateTime()) {
                myLog(tag, "Error! there is no last update time to retrieve"); return
            }
            val dateTimeNow : LocalDateTime = LocalDateTime.now()
            val transitionEvents: List<Transition> =
                getTransitionBetween(lastUpdateTime, dateTimeNow).sortedBy {
                    it.dateTime
                }
            val locContent = loadSharedPrefStr(context, "lastLocation")!!.toLocContent()
            var act = locContent.activityType
            val locId = locContent.locId
            var lat = locContent.lat
            var lon = locContent.lon
            var fromDateTime = lastUpdateTime
            var untilDateTime = dateTimeNow
            if (transitionEvents.isNotEmpty()) {
                transitionEvents.forEach { event ->
                    when (event.transitionType) {
                        ActivityTransition.ACTIVITY_TRANSITION_ENTER -> {
                            fromDateTime = event.dateTime
                            act = event.activityType
                            lat = if (act == DetectedActivity.STILL) { event.latitude } else { null }
                            lon = if (act == DetectedActivity.STILL) { event.longitude } else { null }
                        }
                        ActivityTransition.ACTIVITY_TRANSITION_EXIT -> {
                            untilDateTime = event.dateTime
                            if (event.activityType != act) {
                                act = event.activityType
                                lat = if (act == DetectedActivity.STILL) { event.latitude } else { null }
                                lon = if (act == DetectedActivity.STILL) { event.longitude } else { null }
                            }
                            insertLocationTimeLog(act, locId, lat, lon, fromDateTime, untilDateTime)
                        }
                        else -> { error("ActivityTransition.XXX is invalid (either 0 or 1)") }
                    }
                }
                untilDateTime = dateTimeNow
                insertLocationTimeLog(act, locId, lat, lon, fromDateTime, untilDateTime)
                saveSharedPref(context, "lastLocationUpdatedTime", dateTimeNow.toMilliSec())
                saveSharedPref(context, "lastLocation", "$act,$locId,$lat,$lon")
            } else {
                myLog(tag, "transitionEvents is empty")
                saveSharedPref(context, "lastLocationUpdatedTime", dateTimeNow.toMilliSec())
                insertLocationTimeLog(act, locId, lat, lon, fromDateTime, untilDateTime)

            }
        } else {
            myLog(tag, "subscription to Activity Recognition is nothing")
        }
    }

private fun getOrSetNearLocation(lat: Double, lon: Double): Location {
    val tag = "getNearLocation"
    val nearLocation = getNearLocation(lat, lon)
    return if (nearLocation == null) {
        myLog(tag, "There is no near location saved")
        val location = Location("", lat, lon)
        insertLocation(location)
        location.id = loadSharedPrefInt(application.applicationContext, "locationsNum") + 1
        saveSharedPref(application.applicationContext, "locationsNum", location.id)
        location
    } else {
        myLog(tag, "There is near location saved")
        nearLocation
    }
}

//    private fun getOrSetNearLocation(lat: Double, lon: Double): Location {
//        val tag = "getNearLocation"
//        val allLocations = getAllLocationNotLive()
//        val nearLocations = allLocations.filter { loc ->
//            val dist = (((lat - loc.latitude).pow(2) + (lon - loc.longitude).pow(2)) * (PI / 180 * 6371 * 1000).pow(2)).pow(0.5)
//            myLog(tag, "$lat, $lon, ${loc.latitude}, ${loc.longitude}, $dist")
//            dist < 50
//        }
//
//        return if (nearLocations.isEmpty()) {
//            myLog(tag, "There is no near location saved")
//            val location = Location("", lat, lon)
//            insertLocation(location)
//            location.id = allLocations.size + 1
//            location
//        } else {
//            myLog(tag, "There is near location saved")
//            nearLocations[0]
//        }
//    }

    fun updateSleepLogs(){
        val tag = "updateSleepLogs"
        myLog(tag, "updateSleepLogs called")
        val contentType = ContentType.SLEEP
        val context = application.applicationContext
        val lastUpdateTime: LocalDateTime
            = loadSharedPrefLong(context, "lastSleepUpdatedTime").toLocalDateTime()
        val dateTimeNow : LocalDateTime = LocalDateTime.now()
        val lastSleepState: String = loadSharedPrefStr(context, "lastSleepState")!!
        val sleepEvents: List<Sleep> = getSleepBetween(lastUpdateTime, dateTimeNow)
        var timeContent = lastSleepState
        var fromDateTime = lastUpdateTime
        var untilDateTime = lastUpdateTime

        if (sleepEvents.isNotEmpty()) {
            myLog(tag, "There are ${sleepEvents.size} events")
            sleepEvents.forEach { event ->
                untilDateTime = event.dateTime
                insertTimeLog(
                    TimeLog(
                        contentType = contentType,
                        timeContent = timeContent,
                        fromDateTime = fromDateTime,
                        untilDateTime = untilDateTime
                    )
                )
                fromDateTime = event.dateTime
                timeContent = if (event.confidence >= 70 /*睡眠*/ ) {
                    "sleep"
                } else if (event.confidence <= 30 /*起床*/ ){
                    "awake"
                } else /*不明*/ {
                    "unsure"
                }
            }
            saveSharedPref(context, "lastSleepUpdatedTime", untilDateTime.toMilliSec())
            saveSharedPref(context, "lastSleepState", timeContent)
        } else {
            myLog(tag, "sleepEvents is empty")
        }
    }

    fun updateAll(){
        updateAppLogs()
        updateSleepLogs()
        updateLocationLogs()
    }
}