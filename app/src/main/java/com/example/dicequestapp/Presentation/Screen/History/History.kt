package com.example.dicequestapp.Presentation.Screen.History

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import com.example.dicequestapp.Presentation.Navigation.WithBottomNav
import com.example.dq_ui.Headers.Header
import com.example.dq_ui.R
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.icons.BottomNavItem

@Composable
fun HistoryScreen(
    navController: NavHostController
) {
    WithBottomNav(navController, BottomNavItem.History) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Header(
                text = "История игр",
                leadingIcon = null,
                trailingIcon = null,
                leadingOnClick = {navController.navigate(NavigationRoutes.MAIN)},
                trailingOnClick = {}
            )

            SpacerH(30)

            Text(
                text = "История игр",
                style = DiceQuestTheme.typography.displayLarge,
                color = DiceQuestTheme.colors.TextPrimary
            )

            SpacerH(16)

            Text(
                text = "Здесь будет отображаться история ваших игр",
                style = DiceQuestTheme.typography.bodyMedium,
                color = DiceQuestTheme.colors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            SpacerH(8)

            Text(
                text = "Функция в разработке",
                style = DiceQuestTheme.typography.bodySmall,
                color = DiceQuestTheme.colors.TextSecondary
            )
        }
    }
}