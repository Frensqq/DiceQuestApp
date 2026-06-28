package com.example.dicequestapp.Presentation.Screen.Main.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_ui.Button.ButtonSmall
import com.example.dq_ui.Inputs.InputText
import com.example.dq_ui.Inputs.PlayerCountSelector
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.UI.SpacerW

@Composable
fun CreateGameDialog(
    navHostController: NavHostController,
    viewModel: MainViewModel,
    isMultiplayer: Boolean,
    onDismiss: () -> Unit,
    onGameCreated: (gameId: String) -> Unit
) {
    val state = viewModel.state

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DiceQuestTheme.colors.Surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val typeText = if (isMultiplayer) "многопользовательской" else "одиночной"

                Text(
                    text = "Создание $typeText игры",
                    style = DiceQuestTheme.typography.headlineLarge,
                    color = DiceQuestTheme.colors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                SpacerH(20)

                InputText(
                    text = state.nameGame,
                    placeholder = "Введите название игры",
                    onValueChange = {
                        viewModel.updateState(state.copy(nameGame = it))
                    },
                    isPass = false
                )

                SpacerH(16)

                if (isMultiplayer) {
                    PlayerCountSelector(
                        selectedCount = state.countPlayer,
                        onCountChange = {
                            viewModel.updateState(state.copy(countPlayer = it))
                        }
                    )
                }

                SpacerH(24)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ButtonSmall(
                        onClick = {
                            if (isMultiplayer) {
                                // TODO: Мультиплеер (будет позже)
                                // viewModel.createMultiplayerGame(...)
                            } else {
                                // Одиночная игра с ботами
                                viewModel.createSinglePlayerGame(
                                    gameName = state.nameGame.ifEmpty { "Игра ${System.currentTimeMillis()}" },
                                    botCount = 3
                                ) { gameId ->
                                    onGameCreated(gameId)
                                }
                            }
                        },
                        text = "Создать",
                        type = false
                    )

                    SpacerW(5)

                    ButtonSmall(
                        onClick = {
                            viewModel.updateState(state.copy(nameGame = ""))
                            onDismiss()
                        },
                        text = "Отмена",
                        type = true
                    )
                }
            }
        }
    }
}