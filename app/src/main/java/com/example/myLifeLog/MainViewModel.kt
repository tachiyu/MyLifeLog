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
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.*

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

    private fun getAllLocationNotLive(): List<Location> {
        return locationRepository.getAllLocationNotLive()
    }

    private fun getLastLogInContent(contentType: String): TimeLog? {
        return timeLogRepository.getLastLogInContentType(contentType)
    }

    private fun getFistLog(): TimeLog? {
        return timeLogRepository.getFirstLog()
    }

    fun clearContent(contentType: String) {
        timeLogRepository.clearContent(contentType)
    }

    fun clearTransitionTable() {
        transitionRepository.clearTable()
    }

    private fun getTransitionBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Transition> {
        return transitionRepository.getTransitionBetween(fromDateTime, untilDateTime)
    }

    fun clearSleepTable() {
        sleepRepository.clearTable()
    }

    private fun getSleepBetween(fromDateTime: LocalDateTime, untilDateTime: LocalDateTime): List<Sleep> {
        return sleepRepository.getSleepBetween(fromDateTime, untilDateTime)
    }

    fun getFirstDate(): LocalDate {
        return getFistLog().let {
            if (it!=null) {
                it.fromDateTime.toLocalDate()
            } else {
                LocalDate.now(ZoneId.systemDefault())
            }
        }
    }

    // 最後に保存されたAppログから最新までのAppログを取得しRoomに保存する
    fun updateAppLogs() {
        val tag = "updateAppLogs"
        val contentType = "app"
        Log.d(tag, "updateAppLogs called")
        val usageStatsManager: UsageStatsManager =
            application.applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageEvents: UsageEvents = usageStatsManager.queryEvents(
            getLastLogInContent(contentType)?.untilDateTime?.toMilliSec() ?:0, //untilDateTime of the latest app log or 0
            System.currentTimeMillis()
        )

        var timeContent = ""
        var fromDateTime: LocalDateTime = LocalDateTime.MIN //初期値は使わないがコンパイルエラー回避
        var untilDateTime: LocalDateTime

        var isForeground = false
        while (usageEvents.hasNextEvent()) {
            val event: UsageEvents.Event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

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
    }

    fun insertLocationTimeLog(
        acTyp: Int, locId: Int?, lat: Double?, lon: Double?, fromDateTime: LocalDateTime, untilDateTime: LocalDateTime
    ) {
        var locId = locId
        var lat = lat
        var lon = lon
        if (lat != null && lon != null) {
            val locDb = getOrSetNearLocation(lat, lon)
            locId = locDb.id
            lat = locDb.latitude
            lon = locDb.longitude
        }
        insertTimeLog(
            TimeLog(
            "location",
            "$acTyp,$locId,$lat, $lon",
                fromDateTime,
                untilDateTime
            )
        )
    }

    fun updateLocationLogs(){
        val tag = "updateLocationLogs"
        val contentType = "location"
        Log.d(tag, "updateLocationLogs called")
        val lastUpdateTime: LocalDateTime
            = loadLastUpdateTime(application.applicationContext, contentType)
        if (lastUpdateTime == 0L.toLocalDateTime()) {
            Log.d(tag, "Error! there is no last update time to retrieve"); return
        }
//        val lastLocation: String = loadLastLocation(application.applicationContext)!!
        val dateTimeNow : LocalDateTime = LocalDateTime.now()
        val transitionEvents: List<Transition> =
            getTransitionBetween(lastUpdateTime, dateTimeNow)

//        var timeContent = lastLocation
        val locContent = loadLastLocation(application.applicationContext)!!.toLocContent()
        var act = locContent.activityType
        val locId = locContent.locId
        var lat = locContent.lat
        var lon = locContent.lon
        var fromDateTime = lastUpdateTime
        var untilDateTime = dateTimeNow

        if (loadSetting(application.applicationContext, "IsActivityRecognitionSubscribed")) {
            if (transitionEvents.isNotEmpty()) {
                transitionEvents.forEach { event ->
                    when (event.transitionType) {
                        ActivityTransition.ACTIVITY_TRANSITION_ENTER -> {
                            fromDateTime = event.dateTime
//                            timeContent = if (event.activityType == DetectedActivity.STILL) {
//                                "${event.activityType},${event.latitude},${event.longitude}"
//                            } else {
//                                "${event.activityType},null,null"
//                            }
                            act = event.activityType
                            lat = if (act == DetectedActivity.STILL) { event.latitude } else { null }
                            lon = if (act == DetectedActivity.STILL) { event.longitude } else { null }
                        }
                        ActivityTransition.ACTIVITY_TRANSITION_EXIT -> {
                            untilDateTime = event.dateTime
//                            val (lastActivity, lastLatitude, lastLongitude) = timeContent.parseLocation()
                            if (event.activityType != act) {
//                                timeContent =
//                                    if (event.activityType == DetectedActivity.STILL) {
//                                        "${event.activityType},${event.latitude},${event.longitude}"
//                                    } else {
//                                        "${event.activityType},null,null"
//                                    }
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
                setLastUpdateTime(application.applicationContext, contentType, dateTimeNow)
                setLastLocation(application.applicationContext, "$act,$locId,$lat,$lon")
            } else {
                Log.d(tag, "transitionEvents is empty")
                setLastUpdateTime(application.applicationContext, contentType, dateTimeNow)
                insertLocationTimeLog(act, locId, lat, lon, fromDateTime, untilDateTime)

            }
        } else {
            Log.d(tag, "subscription to Activity Recognition is nothing")
        }
    }

    private fun getOrSetNearLocation(lat: Double, lon: Double): Location {
        val tag = "getNearLocation"
        val allLocations = getAllLocationNotLive()
        val nearLocations = allLocations.filter { loc ->
            val dist2 = 6371 * ((lat/180*PI - loc.latitude /180*PI).pow(2.0) + (lon/180*PI - loc.longitude/180*PI).pow(2.0))
            Log.d(tag, "$lat, $lon, ${loc.latitude}, ${loc.longitude}, $dist2")
            dist2 < (50.0/1000).pow(2.0)
        }
        Log.d(tag, "nearLocations: $nearLocations")

        return if (nearLocations.isEmpty()) {
            Log.d(tag, "There is no near location saved")
            val location = Location("", lat, lon)
            insertLocation(location)
            location.id = allLocations.size + 1
            location
        } else {
            Log.d(tag, "There is near location saved")
            nearLocations[0]
        }
    }

    fun updateSleepLogs(){
        val tag = "updateSleepLogs"
        val contentType = "sleep"
        Log.d(tag, "updateSleepLogs called")
        val lastUpdateTime: LocalDateTime
            = loadLastUpdateTime(application.applicationContext, contentType)
        val dateTimeNow : LocalDateTime = LocalDateTime.now()
        val lastSleepState: String = loadLastSleepState(application.applicationContext)!!
        val sleepEvents: List<Sleep> = getSleepBetween(lastUpdateTime, dateTimeNow)
        var timeContent = lastSleepState
        var fromDateTime = lastUpdateTime
        var untilDateTime = lastUpdateTime

        if (sleepEvents.isNotEmpty()) {
            Log.d(tag, "There are ${sleepEvents.size} events")
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
            setLastUpdateTime(application.applicationContext, contentType, untilDateTime)
            setLastSleepState(application.applicationContext, timeContent)
        } else {
            Log.d(tag, "sleepEvents is empty")
        }
    }

    fun updateAll(){
        updateAppLogs()
        updateSleepLogs()
        updateLocationLogs()
    }
}