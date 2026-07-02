package com.example.dicequestapp.Presentation.Screen.Game

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.WithBottomNav
import com.example.dicequestapp.Presentation.Screen.Main.Component.QRGenerator
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_ui.Button.ButtonBig
import com.example.dq_ui.Headers.Header
import com.example.dq_ui.R
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.icons.BottomNavItem
import kotlinx.coroutines.delay

@Composable
fun WaitingRoomScreen(
    navController: NavHostController,
    viewModel: MainViewModel,
    gameId: String,
    isHost: Boolean,
    onGameStart: () -> Unit
) {
    var playersCount by remember { mutableStateOf(1) }
    var maxPlayers by remember { mutableStateOf(4) }
    var gameStatus by remember { mutableStateOf("waiting") }
    var isLoading by remember { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // QR-код
    val qrBitmap = remember(gameId) {
        QRGenerator.generateQRCode(gameId, 400, 400)
    }

    // Polling
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            viewModel.checkGameStatus(
                gameId = gameId,
                onStatus = { status, players, max ->
                    playersCount = players
                    maxPlayers = max
                    gameStatus = status
                    isLoading = false

                    if (status == "playing") {
                        onGameStart()
                    }
                }
            )
        }
    }

    // Цвета игроков для аватарок
    val playerColors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFFA8E6CF)
    )

    WithBottomNav(navController, BottomNavItem.Home) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SpacerH(10)

            Header(
                text = "Ожидание игроков",
                leadingIcon = painterResource(R.drawable.arrow_left),
                trailingIcon = null,
                leadingOnClick = { navController.popBackStack() },
                {}
            )

            SpacerH(16)

            // Код игры (крупно)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        DiceQuestTheme.colors.Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Код игры",
                        style = DiceQuestTheme.typography.bodySmall,
                        color = DiceQuestTheme.colors.TextSecondary
                    )
                    Text(
                        text = gameId,
                        style = DiceQuestTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        color = DiceQuestTheme.colors.Primary
                    )
                }
            }

            SpacerH(16)

            // Два блока: QR-код + список игроков
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // QR-код
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(2.dp, DiceQuestTheme.colors.Primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR код для приглашения",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Ошибка генерации QR",
                            style = DiceQuestTheme.typography.bodySmall,
                            color = DiceQuestTheme.colors.Error
                        )
                    }
                }

                // Список игроков
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Игроки",
                        style = DiceQuestTheme.typography.titleMedium,
                        color = DiceQuestTheme.colors.TextPrimary
                    )

                    // Заглушка для игроков (пока просто заглушки)
                    for (i in 0 until maxPlayers) {
                        val isConnected = i < playersCount
                        val color = if (isConnected) playerColors[i % playerColors.size] else DiceQuestTheme.colors.SurfaceVariant
                        val name = if (isConnected) {
                            if (i == 0) "Игрок ${i + 1} (Вы)" else "Игрок ${i + 1}"
                        } else {
                            "Свободно"
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        1.5.dp,
                                        if (isConnected) DiceQuestTheme.colors.Primary else DiceQuestTheme.colors.SurfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isConnected) {
                                    Text(
                                        text = "${i + 1}",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Text(
                                        text = "?",
                                        color = DiceQuestTheme.colors.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = name,
                                style = DiceQuestTheme.typography.bodyMedium,
                                color = if (isConnected) DiceQuestTheme.colors.TextPrimary else DiceQuestTheme.colors.TextSecondary
                            )
                        }
                    }
                }
            }

            SpacerH(16)

            // Копирование кода
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ButtonBig(
                    text = "Копировать код",
                    onClick = {
                        clipboardManager.setText(AnnotatedString(gameId))
                    },
                    type = false
                )
            }

            SpacerH(8)

            // Статус и кнопка старта
            if (isLoading) {
                CircularProgressIndicator(
                    color = DiceQuestTheme.colors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            } else if (isHost && playersCount >= 2) {
                ButtonBig(
                    text = "Начать игру!",
                    onClick = {
                        viewModel.startMultiplayerGame(gameId) {
                            onGameStart()
                        }
                    },
                    type = true
                )
            } else if (isHost) {
                Text(
                    text = "Ожидание подключения игроков... (${playersCount}/$maxPlayers)",
                    style = DiceQuestTheme.typography.bodyMedium,
                    color = DiceQuestTheme.colors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Ожидание начала игры...",
                    style = DiceQuestTheme.typography.bodyMedium,
                    color = DiceQuestTheme.colors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            SpacerH(16)
        }
    }
}