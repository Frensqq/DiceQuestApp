package com.example.dicequestapp.Presentation.Screen.Game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import com.example.dicequestapp.Presentation.Navigation.WithBottomNav
import com.example.dicequestapp.Presentation.ViewModels.GameViewModel
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_ui.Button.ButtonBig
import com.example.dq_ui.Button.ButtonSmall
import com.example.dq_ui.Headers.Header
import com.example.dq_ui.R
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.UI.SpacerW
import com.example.dq_ui.icons.BottomNavItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun StartGameScreen(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    val state = viewModel.state
    val mainViewModel: MainViewModel = koinViewModel()
    val context = LocalContext.current

    // Флаг для обновления данных при входе на экран
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isInitialized) {
            // Получаем данные пользователя для отображения аватара
            mainViewModel.GetUser()
            isInitialized = true
        }
    }

    // Если идет загрузка
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = DiceQuestTheme.colors.Primary,
                    modifier = Modifier.size(60.dp)
                )
                SpacerH(16)
                Text(
                    text = "Создание игры...",
                    style = DiceQuestTheme.typography.titleMedium,
                    color = DiceQuestTheme.colors.TextPrimary
                )
            }
        }
        return
    }

    // Если есть ошибка
    if (state.error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ошибка: ${state.error}",
                    style = DiceQuestTheme.typography.titleMedium,
                    color = DiceQuestTheme.colors.Error,
                    textAlign = TextAlign.Center
                )
                SpacerH(16)
                ButtonSmall(
                    onClick = {
                        navController.popBackStack()
                    },
                    text = "Назад",
                    type = true
                )
            }
        }
        return
    }

    // Основной экран
    WithBottomNav(navController, BottomNavItem.Home) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SpacerH(10)

            Header(
                text = "Начало игры",
                leadingIcon = painterResource(R.drawable.arrow_left),
                trailingIcon = null,
                leadingOnClick = {
                    navController.popBackStack()
                },
                trailingOnClick = {}
            )

            SpacerH(20)

            // Информация об игре
            Text(
                text = state.game?.name ?: "Игра",
                style = DiceQuestTheme.typography.displayLarge,
                color = DiceQuestTheme.colors.TextPrimary,
                textAlign = TextAlign.Center
            )

            SpacerH(8)

            Text(
                text = if (state.isMultiplayer) "Мультиплеер" else "Одиночная игра",
                style = DiceQuestTheme.typography.titleMedium,
                color = DiceQuestTheme.colors.TextSecondary
            )

            SpacerH(24)

            // Статистика по игрокам
            PlayerStatistics(
                state = state,
                mainViewModel = mainViewModel
            )

            SpacerH(24)

            // Статистика по ячейкам
            CellStatistics(state)

            SpacerH(24)

            // Кнопка старта
            StartButton(
                canStart = state.canStart,
                onStart = {
                    // Переходим на экран игрового поля
                    navController.navigate(NavigationRoutes.GAME_BOARD) {
                        popUpTo(NavigationRoutes.START_GAME) { inclusive = true }
                    }
                }
            )

            SpacerH(16)

            // Информация о статусе
            StatusInfo(state)
        }
    }
}

@Composable
fun PlayerStatistics(
    state: com.example.dicequestapp.Presentation.State.GameState,
    mainViewModel: MainViewModel
) {
    val userState = mainViewModel.state

    // Получаем аватар текущего пользователя
    val userAvatarUrl = userState.User?.let { user ->
        if (user.avatar.isNotEmpty()) {
            mainViewModel.getImageUrl("users", user.id, user.avatar)
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DiceQuestTheme.colors.SurfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text = "Игроки",
            style = DiceQuestTheme.typography.titleMedium,
            color = DiceQuestTheme.colors.TextPrimary
        )

        SpacerH(12)

        // Список игроков
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Текущий игрок (Вы)
            PlayerItem(
                name = UserRepository.userName ?: "Игрок",
                avatarUrl = userAvatarUrl,
                isYou = true,
                isBot = false,
                status = "Готов"
            )

            // Остальные игроки (пока заглушки)
            for (i in 1 until state.expectedPlayers) {
                // Для мультиплеера - показываем как ботов
                // В будущем здесь будут реальные игроки
                val isBot = true
                val botName = "Bot $i"

                PlayerItem(
                    name = if (isBot) botName else "Игрок ${i + 1}",
                    avatarUrl = null,
                    isYou = false,
                    isBot = isBot,
                    status = if (isBot) "Готов" else "Ожидание..."
                )
            }
        }

        SpacerH(8)

        Text(
            text = "Подключено: ${state.currentPlayers + 1} / ${state.expectedPlayers}",
            style = DiceQuestTheme.typography.bodySmall,
            color = DiceQuestTheme.colors.TextSecondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlayerItem(
    name: String,
    avatarUrl: String?,
    isYou: Boolean,
    isBot: Boolean,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isBot) {
                // Для ботов - стандартная иконка
                Image(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = "Bot avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DiceQuestTheme.colors.SurfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else if (avatarUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = avatarUrl),
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DiceQuestTheme.colors.SurfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = "Default avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DiceQuestTheme.colors.SurfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }
        }

        SpacerW(12)

        // Имя и статус
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = DiceQuestTheme.typography.titleMedium,
                    color = DiceQuestTheme.colors.TextPrimary
                )

                if (isYou) {
                    SpacerW(8)
                    Text(
                        text = "(Вы)",
                        style = DiceQuestTheme.typography.bodySmall,
                        color = DiceQuestTheme.colors.Primary
                    )
                }

                if (isBot) {
                    SpacerW(8)
                    Text(
                        text = "🤖",
                        style = DiceQuestTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = status,
                style = DiceQuestTheme.typography.bodySmall,
                color = if (status == "Готов") {
                    DiceQuestTheme.colors.Success
                } else {
                    DiceQuestTheme.colors.TextSecondary
                }
            )
        }

        // Индикатор статуса
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    if (status == "Готов") {
                        DiceQuestTheme.colors.Success
                    } else {
                        DiceQuestTheme.colors.TextSecondary.copy(alpha = 0.3f)
                    }
                )
        )
    }
}

@Composable
fun CellStatistics(state: com.example.dicequestapp.Presentation.State.GameState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DiceQuestTheme.colors.SurfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text = "Состав поля",
            style = DiceQuestTheme.typography.titleMedium,
            color = DiceQuestTheme.colors.TextPrimary
        )

        SpacerH(8)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Бонус",
                count = state.bonusCount,
                color = Color(0xFF4CAF50)
            )
            StatItem(
                label = "Штраф",
                count = state.penaltyCount,
                color = Color(0xFFE53935)
            )
            StatItem(
                label = "Защита",
                count = state.protectionCount,
                color = Color(0xFF2196F3)
            )
            StatItem(
                label = "Событие",
                count = state.eventCount,
                color = Color(0xFFFFC107)
            )
            StatItem(
                label = "Пустые",
                count = state.emptyCount,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun StatItem(label: String, count: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = DiceQuestTheme.typography.titleLarge,
            color = color
        )
        Text(
            text = label,
            style = DiceQuestTheme.typography.bodySmall,
            color = DiceQuestTheme.colors.TextSecondary
        )
    }
}

@Composable
fun StartButton(canStart: Boolean, onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ButtonBig(
            text = if (canStart) "Начать игру!" else "Ожидание игроков...",
            onClick = onStart,
            enabled = canStart,
            type = true
        )

        if (!canStart) {
            SpacerH(8)
            Text(
                text = "Дождитесь подключения всех игроков",
                style = DiceQuestTheme.typography.bodySmall,
                color = DiceQuestTheme.colors.TextSecondary
            )
        }
    }
}

@Composable
fun StatusInfo(state: com.example.dicequestapp.Presentation.State.GameState) {
    if (state.gameStarted) {
        Text(
            text = "Игра началась! 🎮",
            style = DiceQuestTheme.typography.titleMedium,
            color = DiceQuestTheme.colors.Success
        )
    } else {
        Text(
            text = "Ожидание начала игры...",
            style = DiceQuestTheme.typography.bodyMedium,
            color = DiceQuestTheme.colors.TextSecondary
        )
    }
}