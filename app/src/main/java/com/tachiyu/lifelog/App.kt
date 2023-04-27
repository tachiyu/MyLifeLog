package com.tachiyu.lifelog
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.tachiyu.lifelog.ui.MyNavHost

@Composable
fun App(viewModel: MainViewModel) {
    val navController = rememberNavController()
    MyNavHost(viewModel = viewModel,
        navController = navController)
}

