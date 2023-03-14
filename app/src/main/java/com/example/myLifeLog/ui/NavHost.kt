package com.example.myLifeLog.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myLifeLog.MainViewModel
import com.example.myLifeLog.myLog

enum class DESTINATIONS() {
    SUMMARY(),
    INPUT(),
    SETTING(),
    NAME()
}

@Composable
fun MyNavHost(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "${DESTINATIONS.SUMMARY}/{args}"
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable("${DESTINATIONS.INPUT}/{args}") { backStackEntry ->
            val args = backStackEntry.arguments!!.getString("args")!!.split(",")
            InputScreenSetup(
                viewModel = viewModel,
                navController = navController,
                periodTabSelected = args[0].toInt(),
                contentTabSelected = args[1].toInt()
            )
        }
        composable("${DESTINATIONS.SUMMARY}/{args}") { backStackEntry ->
            if (backStackEntry.arguments?.getString("args") != null) {
                myLog("backstack", "not null")
                val args = backStackEntry.arguments!!.getString("args")!!.split(",")
                SummaryScreenSetup(
                    viewModel = viewModel,
                    navController = navController,
                    period0 = args[0].toInt(),
                    contentType0 = args[1].toInt()
                )
            } else {
                myLog("backstack", "null")
                SummaryScreenSetup(
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
        composable("${DESTINATIONS.SETTING}") {
            SettingScreenSetup(
                viewModel = viewModel
            )
        }
        composable("${DESTINATIONS.NAME}/{args}") { backStackEntry ->
            val args = backStackEntry.arguments!!.getString("args")!!.split(",")
            NameScreen(
                viewModel = viewModel,
                navController = navController,
                locId = args[0].toInt(),
                lat = args[1].toDouble(),
                lon = args[2].toDouble(),
                period = args[3].toInt(),
                contentType = args[4].toInt()
            )
        }
    }
}