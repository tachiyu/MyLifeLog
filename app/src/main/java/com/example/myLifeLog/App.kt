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

//@Composable
//fun NavBottomBar(navController: NavHostController) {
//    val tabs = DESTINATIONS.values().slice(0..1)
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//    var cnt by remember{ mutableStateOf(0) } // 設定画面に映るためのカウンタ（隠しコマンド）
//
//    BottomNavigation (
//        modifier = Modifier,
//            ){
//        tabs.forEach { tab ->
//            BottomNavigationItem(
//                selected = tab.str == currentRoute,
//                onClick = {
//                    if (tab.str != currentRoute) {
//                        navController.navigate(tab.str)
//                    } else /*同じタブを20回押すと設定画面に*/{
//                        if (cnt >= 20) {
//                            navController.navigate(DESTINATIONS.SETTING.str)
//                            cnt = 0
//                        } else {
//                            cnt ++
//                        }
//                    }
//                },
//                icon = {},
//                label = {Text(text = stringResource(tab.shown))})
//        }
//    }
//}
