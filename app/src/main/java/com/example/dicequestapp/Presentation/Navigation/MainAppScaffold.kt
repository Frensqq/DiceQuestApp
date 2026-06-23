package com.example.dicequestapp.Presentation.Navigation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.navigation.NavHostController
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.icons.BottomNavItem
import com.example.dq_ui.icons.BottomNavigationPanel

@Composable
fun WithBottomNav(
    navController: NavHostController,
    currentScreen: BottomNavItem,
    content: @Composable () -> Unit
) {
    var selectedItem by remember { mutableStateOf(currentScreen) }

    Scaffold(
        modifier = Modifier.background(Transparent),
        bottomBar = {
            BottomNavigationPanel(
                selectedItem = selectedItem,
                onItemSelected = { item ->
                    selectedItem = item
                    when (item) {
                        BottomNavItem.Home -> navController.navigate(NavigationRoutes.MAIN) {
                            popUpTo(NavigationRoutes.MAIN) { inclusive = true }
                        }
                        BottomNavItem.Home -> navController.navigate(NavigationRoutes.MAIN)
                        BottomNavItem.Profile -> navController.navigate(NavigationRoutes.PROFILE)
                        BottomNavItem.History ->  navController.navigate(NavigationRoutes.HISTORY)
                        BottomNavItem.Settings -> navController.navigate(NavigationRoutes.SETTINGS)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(
                    DiceQuestTheme.colors.Primary,
                    DiceQuestTheme.colors.SurfaceVariant
                )))
                .padding(paddingValues)
        ) {
            content()
        }
    }
}