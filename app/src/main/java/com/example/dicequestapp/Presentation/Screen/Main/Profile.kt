package com.example.dicequestapp.Presentation.Screen.Main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import com.example.dicequestapp.Presentation.Navigation.WithBottomNav
import com.example.dicequestapp.Presentation.Screen.Main.Component.EditProfileDialog
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_net_library.Domain.Repository.Repository
import com.example.dq_ui.Cards.CardMenu
import com.example.dq_ui.Cards.CardUser
import com.example.dq_ui.Headers.Header
import com.example.dq_ui.R
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.icons.BottomNavItem
import com.example.htm.Presentation.viewModels.AuthViewModel

@Composable
fun ProfileScreen(navController: NavHostController, viewModel: MainViewModel){

    val state = viewModel.state
    val UserData = state.User

    var showEditDialog by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        if (loading) {
            viewModel.GetUser()
            loading = false
        }
    }

    WithBottomNav(navController, BottomNavItem.Profile){
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

            SpacerH(10)
            Header(
                "Профиль",
                painterResource(R.drawable.arrow_left),
                painterResource(R.drawable.redact),
                {
                    navController.navigate(NavigationRoutes.MAIN)
                },
                trailingOnClick = {
                    showEditDialog = true
                }
            )

            SpacerH(30)

            // Аватар пользователя
            val userAvatarUrl = UserData?.let { user ->
                if (user.avatar.isNotEmpty()) {
                    viewModel.getImageUrl("users", user.id, user.avatar)
                } else null
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (userAvatarUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = userAvatarUrl),
                        contentDescription = "avatar",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.profile),
                        contentDescription = "default avatar",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                    )
                }
                Text(
                    text = UserRepository.userName,
                    style = DiceQuestTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    color = DiceQuestTheme.colors.TextPrimary,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            SpacerH(30)

            Text(
                text = "История игр",
                style = DiceQuestTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = DiceQuestTheme.colors.TextPrimary,
                modifier = Modifier.padding(top = 5.dp)
            )

            SpacerH(5)

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(CircleShape)
                .background(DiceQuestTheme.colors.Primary)
            )



        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            navController,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false }
        )
    }

}