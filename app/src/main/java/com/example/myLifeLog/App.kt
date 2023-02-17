package com.example.myLifeLog
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.myLifeLog.ui.MyNavHost

@Composable
fun App(viewModel: MainViewModel) {
    val navController = rememberNavController()
    MyNavHost(viewModel = viewModel,
        navController = navController)
}

