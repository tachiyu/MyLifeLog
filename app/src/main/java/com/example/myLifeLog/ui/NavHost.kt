package com.example.myLifeLog.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myLifeLog.MainViewModel

enum class DESTINATIONS(val str: String) {
    SUMMARY("summary"),
    INPUT("input"),
    SETTING("setting"),
    NAME("name")
}

@Composable
fun MyNavHost(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "${DESTINATIONS.SUMMARY.str}/{args}"
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable("${DESTINATIONS.INPUT.str}/{args}") { backStackEntry ->
            val args = backStackEntry.arguments!!.getString("args")!!.split(",")
            InputScreenSetup(
                viewModel = viewModel,
                navController = navController,
                periodTabSelected = args[0].toInt(),
                contentTabSelected = args[1].toInt()
            )
        }
        composable("${DESTINATIONS.SUMMARY.str}/{args}") { backStackEntry ->
            if (backStackEntry.arguments?.getString("args") != null) {
                val args = backStackEntry.arguments!!.getString("args")!!.split(",")
                SummaryScreenSetup(
                    viewModel = viewModel,
                    navController = navController,
                    periodTabSelected = args[0].toInt(),
                    contentTabSelected = args[1].toInt()
                )
            } else {
                SummaryScreenSetup(
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
        composable(DESTINATIONS.SETTING.str) {
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
                periodTabSelected = args[3].toInt(),
                contentTabSelected = args[4].toInt()
            )
        }
    }
}