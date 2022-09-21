package com.example.timeitforward

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.timeitforward.ui.DESTINATIONS
import com.example.timeitforward.ui.MyNavHost

@Composable
fun App(viewModel: TimeLogViewModel) {
    val navController = rememberNavController()
    Scaffold(bottomBar = { NavBottomBar(navController = navController) }) {
        innerPaddingModifier ->
        MyNavHost(viewModel = viewModel,
            navController = navController,
            modifier = Modifier.padding(innerPaddingModifier))
    }
}

@Composable
fun NavBottomBar(navController: NavHostController) {
    val tabs = DESTINATIONS.values()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
//        ?: CourseTabs.FEATURED.route

    BottomNavigation (
        modifier = Modifier,
            ){
        tabs.forEach { tab ->
            BottomNavigationItem(
                selected = tab.str == currentRoute,
                onClick = {
                    if (tab.str != currentRoute) {
                        navController.navigate(tab.str)
                    }
                },
                icon = {},
                label = {Text(text = stringResource(tab.shown))})
        }
    }
}