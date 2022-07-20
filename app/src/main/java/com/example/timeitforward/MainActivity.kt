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
import com.vmadalin.easypermissions.EasyPermissions

class MainActivity : ComponentActivity(), EasyPermissions.PermissionCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permission = Permission(this)
        permission.requestUsageStatsPermission()
        permission.requestActivityRecognitionPermission()
        permission.requestBackgroundLocationPermission()

        val appLog = AppLog(this)
        appLog.readUsageEventsAsTimeLogs()

        setContent {
            TimeItForwardTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    val owner = LocalViewModelStoreOwner.current

                    owner?.let {
                        val viewModel: TimeLogViewModel = viewModel(
                            it,
                            "MainViewModel",
                            TimeLogViewModelFactory(
                                LocalContext.current.applicationContext
                                        as Application
                            )
                        )
                        loadLogs(viewModel)
                        InputScreenSetup(viewModel)
                    }
                }
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
    }
}

class TimeLogViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimeLogViewModel(application) as T
    }
}

fun loadLogs(viewModel: TimeLogViewModel) {
    loadSleepLogs(viewModel = viewModel)
}