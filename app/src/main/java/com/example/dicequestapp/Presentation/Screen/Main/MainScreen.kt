package com.example.dicequestapp.Presentation.Screen.Main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.WithBottomNav
import com.example.dq_ui.Cards.CardMenu
import com.example.dq_ui.Cards.CardUser
import com.example.dq_ui.Headers.Header
import com.example.dq_ui.R
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.icons.BottomNavItem
import com.example.htm.Presentation.viewModels.AuthViewModel

@Composable
fun MainScreen(navController: NavHostController, viewModel: AuthViewModel){
    val state = viewModel.state




    WithBottomNav(navController, BottomNavItem.Home){
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

            SpacerH(10)
            CardUser(
                null,
                UserRepository.userName?:"User",
                {},
                painterResource(R.drawable.settings)
            )


            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                CardMenu(
                    "Быстрая игра",
                    "Сыграйте в игру с ботом",
                    {},
                    painterResource( com.example.dicequestapp.R.drawable.one_player)
                )
                SpacerH(20)
                CardMenu(
                    "Мультиплеер",
                    "Сыграйте в игру с друзьями от 2 до 4 человек",
                    {},
                    painterResource(com.example.dicequestapp.R.drawable.multi_player)
                )
                SpacerH(20)
                CardMenu("Правила игры", "Изучите правила и особенности игры перед началом", {}, painterResource(com.example.dicequestapp.R.drawable.rule))
            }

        }
    }


}