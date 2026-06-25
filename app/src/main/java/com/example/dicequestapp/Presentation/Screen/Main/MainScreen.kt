package com.example.dicequestapp.Presentation.Screen.Main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import com.example.dicequestapp.Presentation.Navigation.WithBottomNav
import com.example.dicequestapp.Presentation.Screen.Main.Component.CreateGameDialog
import com.example.dicequestapp.Presentation.Screen.Main.Component.EditProfileDialog
import com.example.dicequestapp.Presentation.Screen.Main.Component.JoinGameDialog
import com.example.dicequestapp.Presentation.ViewModels.GameViewModel
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_ui.Cards.CardMenu
import com.example.dq_ui.Cards.CardUser
import com.example.dq_ui.Headers.Header
import com.example.dq_ui.R
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.icons.BottomNavItem
import com.example.htm.Presentation.viewModels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(navController: NavHostController, viewModel: MainViewModel, gameViewModel: GameViewModel){
    val state = viewModel.state
    val UserData = state.User

    var showEditDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateGameDialog by remember { mutableStateOf(false) }
    var isMultiplayer by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        if (loading) {
            viewModel.GetUser()
            loading = false
        }
    }

    val userAvatarUrl = UserData?.let { user ->
        if (user.avatar.isNotEmpty()) {
            viewModel.getImageUrl("users", user.id, user.avatar)
        } else null
    }

    WithBottomNav(navController, BottomNavItem.Home){
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

            SpacerH(10)
            CardUser(
                rememberAsyncImagePainter(userAvatarUrl),
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
                    {
                        isMultiplayer = false
                        showCreateGameDialog = true
                    },
                    painterResource(com.example.dicequestapp.R.drawable.one_player)
                )
                SpacerH(20)
                CardMenu(
                    "Мультиплеер",
                    "Сыграйте в игру с друзьями от 2 до 4 человек",
                    {
                        isMultiplayer = true
                        showCreateGameDialog = true
                    },
                    painterResource(com.example.dicequestapp.R.drawable.multi_player)
                )
                SpacerH(20)
                CardMenu(
                    "Войти в игру",
                    "Войти в уже созданную игру",
                    {
                        showJoinDialog = true
                    },
                    painterResource(com.example.dicequestapp.R.drawable.join_game))
                SpacerH(20)
                CardMenu(
                    "Правила игры",
                    "Изучите правила и особенности игры перед началом",
                    {},
                    painterResource(com.example.dicequestapp.R.drawable.rule)
                )
            }
        }
    }

    if (showCreateGameDialog) {
        CreateGameDialog(
            navController,
            viewModel = viewModel,
            gameViewModel = gameViewModel,
            isMultiplayer = isMultiplayer,
            onDismiss = { showCreateGameDialog = false },
            onGameCreated = {
                showCreateGameDialog = false
                navController.navigate(NavigationRoutes.START_GAME)
            }
        )
    }

    if (showEditDialog) {
        EditProfileDialog(
            navController,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false }
        )
    }

    if (showJoinDialog) {
        JoinGameDialog(
            navController,
            viewModel = viewModel,
            onDismiss = { showJoinDialog = false }
        )
    }
}