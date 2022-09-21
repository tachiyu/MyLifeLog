package com.example.timeitforward

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timeitforward.ui.theme.TimeItForwardTheme

class MainActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // パーミッションの確認・パーミッションが無ければユーザーに許可を求める
        val permission = Permission(this)
        permission.requestUsageStatsPermission()
        permission.requestActivityRecognitionPermission()
        permission.requestBackgroundLocationPermission()

        setContent {
            TimeItForwardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    val owner = LocalViewModelStoreOwner.current

                    owner?.let {
                        // viewModelの作製・Appの起動
                        val viewModel: TimeLogViewModel = viewModel(
                            it,
                            "MainViewModel",
                            TimeLogViewModelFactory(
                                LocalContext.current.applicationContext
                                        as Application
                            )
                        )
                        App(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

class TimeLogViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimeLogViewModel(application) as T
    }
}
