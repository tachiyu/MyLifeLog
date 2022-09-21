package com.example.timeitforward.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timeitforward.InputScreenSetup
import com.example.timeitforward.R
import com.example.timeitforward.TimeLogViewModel

enum class DESTINATIONS(val str: String, val shown: Int) {
    INPUT("input", R.string.INPUT),
    SUMMARY("summary", R.string.SUMMARY)
}

@Composable
fun MyNavHost(
    viewModel: TimeLogViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DESTINATIONS.INPUT.str
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
    }
}