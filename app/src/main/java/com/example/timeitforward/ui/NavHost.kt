package com.example.timeitforward.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timeitforward.InputScreenSetup
import com.example.timeitforward.R
import com.example.timeitforward.MainViewModel

enum class DESTINATIONS(val str: String, val shown: Int) {
    SUMMARY("summary", R.string.SUMMARY),
    INPUT("input", R.string.INPUT),
    SETTING("setting", R.string.SETTING)
}

@Composable
fun MyNavHost(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DESTINATIONS.SUMMARY.str
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(DESTINATIONS.INPUT.str) {
            InputScreenSetup(
                viewModel = viewModel
            )
        }
        composable(DESTINATIONS.SUMMARY.str) {
            SummaryScreenSetup(
                viewModel = viewModel
            )
        }
        composable(DESTINATIONS.SETTING.str) {
            SettingScreenSetup(
                viewModel = viewModel
            )
        }
    }
}