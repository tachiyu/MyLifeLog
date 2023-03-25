package com.example.myLifeLog

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.myLifeLog.model.apimanager.ActivityTransitionManager
import com.example.myLifeLog.model.apimanager.SleepManager
import com.example.myLifeLog.model.db.MainRoomDatabase
import com.example.myLifeLog.model.db.location.LocationRepository
import com.example.myLifeLog.model.db.others.OthersRepository
import com.example.myLifeLog.model.db.sleep.SleepRepository
import com.example.myLifeLog.model.db.transition.TransitionRepository

// ViewModelクラス
class MainViewModel(private val application: Application) : ViewModel() {

    // 各APIマネージャーの初期化
    private val activityTransitionManager = ActivityTransitionManager.getInstance(application)
    private val sleepManager = SleepManager.getInstance(application)

    // 各リポジトリの初期化
    private val locationRepository: LocationRepository
    private val transitionRepository: TransitionRepository
    private val sleepRepository: SleepRepository
    private val othersRepository: OthersRepository

    init {
        val db = MainRoomDatabase.getInstance(application)
        val locationDao = db.LocationDao()
        val transitionDao = db.TransitionDao()
        val sleepDao = db.SleepDao()
        val othersDao = db.OthersDao()
        locationRepository = LocationRepository(locationDao)
        transitionRepository = TransitionRepository(transitionDao)
        sleepRepository = SleepRepository(sleepDao)
        othersRepository = OthersRepository(othersDao)
    }

    fun subscribeActivity() {
        activityTransitionManager.subscribeActivity()
    }

    fun subscribeSleep() {
        sleepManager.subscribeActivity()
    }

    // すべてのLocationを取得
    fun getAllLocations(): List<Location> {
        return locationRepository.getAllLocations()
    }

    // すべてのSleepを取得
    fun getAllSleeps(): List<Sleep> {
        return sleepRepository.getAllSleeps()
    }

    // すべてのTransitionを取得
    fun getAllTransitions(): List<Transition> {
        return transitionRepository.getAllTransition()
    }

    // IDによるLocationの取得
    fun getLocationById(id: Int): Location? {
        return locationRepository.getLocationById(id)
    }

    // Othersの挿入
    fun insertOthers(others: Others) {
        othersRepository.insertOthers(others)
    }

    // Locationの更新
    fun updateLocation(location: Location) {
        locationRepository.updateLocation(location)
    }

    // 指定期間のTransitionを取得
    fun getTransitionLogsInRangeWithNearestPrevious(fromDateTime: Long, untilDateTime: Long): List<Transition> {
        return transitionRepository.getTransitionLogsInRangeWithNearestPrevious(fromDateTime, untilDateTime)
    }

    // 指定期間のSleepを取得
    fun getSleepLogsInRangeWithNearestPrevious(fromDateTime: Long, untilDateTime: Long): List<Sleep> {
        return sleepRepository.getSleepLogsInRangeWithNearestPrevious(fromDateTime, untilDateTime)
    }

    // 指定期間のOthersを取得
    fun getOthersLogsInRange(fromDateTime: Long, untilDateTime: Long): List<Others> {
        return othersRepository.getOthersLogsInRange(fromDateTime, untilDateTime)
    }

    // 指定期間のアプリケーションログを取得
    fun getAppLogs(from: Long, until: Long): List<App> {
        val apps = mutableListOf<App>()
        val usageStatsManager: UsageStatsManager =
            application.applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageEvents: UsageEvents = usageStatsManager.queryEvents(from, until)
        val event: UsageEvents.Event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (
                "launcher" !in event.packageName
                && event.packageName != application.packageName
                && event.eventType in listOf<Int>(UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.ACTIVITY_PAUSED)
            ) {
                apps.add(
                    App(
                        event.packageName,
                        event.timeStamp,
                        event.eventType
                    )
                )
            }
        }
        return apps
    }

    // コンテンツタイプに応じたログを取得
    fun getLogs(contentType: Int, from: Long, until: Long): List<TimeLog> {
        return when(contentType) {
            ContentType.APP -> {
                getAppLogs(from, until).toTimeLogList(from, until)
            }
            ContentType.LOCATION -> {
                getTransitionLogsInRangeWithNearestPrevious(from, until).toTimeLogList(from, until)
            }
            ContentType.SLEEP -> {
                getSleepLogsInRangeWithNearestPrevious(from, until).toTimeLogList(from, until)
            }
            else -> {
                getOthersLogsInRange(from, until).toTimeLogList(from, until)
            }
        }
    }
}
