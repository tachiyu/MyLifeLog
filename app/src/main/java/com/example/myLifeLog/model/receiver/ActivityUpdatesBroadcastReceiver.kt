package com.example.myLifeLog.model.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myLifeLog.*
import com.example.myLifeLog.model.checkLocationPermission
import com.example.myLifeLog.model.db.MainRoomDatabase
import com.example.myLifeLog.model.db.location.LocationRepository
import com.example.myLifeLog.model.db.transition.TransitionRepository
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private const val TAG = "ActivityUpdatesBroadcastReceiver"

class ActivityUpdatesBroadcastReceiver : BroadcastReceiver() {

    // このレシーバがブロードキャストメッセージを受信したときに呼び出されるメソッド
    override fun onReceive(context: Context, intent: Intent) {
        // ActivityTransitionResult の結果がある場合に処理を行う
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            myLog(TAG, "receive ${result.transitionEvents.size} transition events", context)

            // データベースとリポジトリの初期化
            val dB = MainRoomDatabase.getInstance(context)
            val locationRepository = LocationRepository(dB.LocationDao())
            val transitionRepository = TransitionRepository(dB.TransitionDao())
            val locationClient = LocationServices.getFusedLocationProviderClient(context)

            // すべての遷移イベントを処理する
            for (event in result.transitionEvents) {
                processEvent(context, event, locationClient, locationRepository, transitionRepository)
            }
        }
    }

    // アクティビティ遷移イベントを処理するメソッド
    private fun processEvent(
        context: Context,
        event: ActivityTransitionEvent,
        locationClient: FusedLocationProviderClient,
        locationRepository: LocationRepository,
        transitionRepository: TransitionRepository
    ) {
        // イベントの詳細をログに出力
        myLog(TAG, "get event: ActivityType=${event.activityType}, TransitionType=${event.transitionType}", context)
        val dateTimeNow = LocalDateTime.now().toMilliSec()

        // イベントが静止状態で、遷移タイプがエンターの場合、位置情報を処理する
        if (event.activityType == DetectedActivity.STILL && event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
            processStillTransition(context, event, locationClient, locationRepository, transitionRepository, dateTimeNow)
        } else {
            // それ以外の場合、遷移イベントをデータベースに挿入する
            CoroutineScope(Dispatchers.IO).launch {
                insertTransition(transitionRepository, event, dateTimeNow)
            }
        }
    }

    // DetectedActivity.STILL の遷移イベントを処理するメソッド
    private fun processStillTransition(
        context: Context,
        event: ActivityTransitionEvent,
        locationClient: FusedLocationProviderClient,
        locationRepository: LocationRepository,
        transitionRepository: TransitionRepository,
        dateTimeNow: Long
    ) {
        // 位置情報を取得し、成功時と失敗時にそれぞれ対応する処理を行う
        doSomethingWithLocation(
            context, locationClient, 1, 5,
            onSuccess = { location ->
                // 位置情報が取得できた場合、位置IDを取得して遷移イベントをデータベースに挿入する
                CoroutineScope(Dispatchers.IO).launch {
                    val locationId = getLocationId(location, locationRepository)
                    insertTransition(transitionRepository, event, dateTimeNow, locationId)
                }
            },
            onFailure = {
                // 位置情報が取得できなかった場合、遷移イベントをデータベースに挿入する（位置情報なし）
                CoroutineScope(Dispatchers.IO).launch {
                    insertTransition(transitionRepository, event, dateTimeNow)
                }
            }
        )
    }

    private suspend fun getLocationId(
        location: android.location.Location,
        locationRepository: LocationRepository
    ): Int {
        // 近くの位置情報をデータベースから取得する
        val nearLocation = locationRepository.getNearLocation(location.latitude, location.longitude)

        // 既存の位置情報があればそのIDを使用し、なければ新たに位置情報をデータベースに挿入してIDを取得する
        return nearLocation?.id ?: createAndInsertNewLocation(location, locationRepository)
    }

    private suspend fun createAndInsertNewLocation(
        location: android.location.Location,
        locationRepository: LocationRepository
    ): Int {
        return locationRepository.insertLocationAndGetId(
            Location(
                name = "",
                latitude = location.latitude,
                longitude = location.longitude
            )
        ).toInt()
    }

    // 遷移イベントをデータベースに挿入するメソッド
    private suspend fun insertTransition(
        transitionRepository: TransitionRepository,
        event: ActivityTransitionEvent,
        dateTime: Long,
        locationId: Int? = null
    ) {
        // 遷移イベントのデータを作成し、データベースに挿入する
        transitionRepository.insertTransition(
            Transition(
                activityType = event.activityType,
                transitionType = event.transitionType,
                locationId = locationId,
                dateTime = dateTime
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun doSomethingWithLocation(context: Context,
                                        locationClient: FusedLocationProviderClient,
                                        cnt: Int, //この関数の試行回数
                                        maxCnt: Int, //cntがこの値を超えると、試行をあきらめる
                                        onSuccess: (android.location.Location) -> Unit,
                                        onFailure: () -> Unit ){
        val tag = "doSomethingWithLocation"
        // check permission
        if (checkLocationPermission(context)) {
            myLog(tag, "try to get location (${cnt}th time", context)
            locationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                }
            ).addOnSuccessListener { location ->
                if (location != null) {
                    myLog(tag, "get location: ${location.latitude} ${location.longitude}", context)
                    onSuccess(location)
                } else {
                    myLog(tag, "get null location", context)
                    // cnt が maxCntを超えていなければ、再帰的に再試行。
                    if (cnt < maxCnt) {
                        doSomethingWithLocation(
                            context, locationClient, cnt+1, maxCnt, onSuccess, onFailure
                        )
                    } else {
                        onFailure()
                    }
                }
            }.addOnFailureListener {
                myLog(tag, "can not get location", context)
                // cnt が maxCntを超えていなければ、再帰的に再試行。
                if (cnt < maxCnt) {
                    doSomethingWithLocation(
                        context, locationClient, cnt+1, maxCnt, onSuccess, onFailure
                    )
                } else {
                    onFailure()
                }
            }
        } else {
            myLog(tag, "permission denied (either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION is required)", context)
            return
        }
    }

    companion object {
        // アクティビティ遷移アップデートのアクション定数
        const val ACTION_PROCESS_UPDATES =
            "com.example.myLifeLog.action.TRANSITION_UPDATES"
    }
}

