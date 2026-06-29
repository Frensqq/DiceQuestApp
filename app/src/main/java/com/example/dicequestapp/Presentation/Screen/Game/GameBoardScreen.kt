package com.example.dicequestapp.Presentation.Screen.Game

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.WithBottomNav
import com.example.dicequestapp.Presentation.ViewModels.GameViewModel
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Player.Player
import com.example.dq_ui.Cards.CardEvents
import com.example.dq_ui.Dice.Dice
import com.example.dq_ui.Field.GameCell
import com.example.dq_ui.Field.GameFieldCell
import com.example.dq_ui.Headers.Header
import com.example.dq_ui.R
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.icons.BottomNavItem
import kotlinx.coroutines.delay

private const val COLS = 7

private fun Cell.cellColor(): Color = when (type) {
    "start" -> DiceQuestTheme.colors.Primary.copy(alpha = 0.3f)
    "finish" -> DiceQuestTheme.colors.Primary.copy(alpha = 0.3f)
    "bonus" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
    "penalty" -> Color(0xFFE53935).copy(alpha = 0.2f)
    "protection" -> Color(0xFF2196F3).copy(alpha = 0.2f)
    "event" -> Color(0xFFFFC107).copy(alpha = 0.2f)
    else -> DiceQuestTheme.colors.Surface
}

private fun getLogColor(message: String): Color {
    return when {
        message.contains("Бонус") || message.contains("+") -> Color(0xFF4CAF50)
        message.contains("Штраф") || message.contains("-") -> Color(0xFFE53935)
        message.contains("Защита") -> Color(0xFF2196F3)
        message.contains("победил") || message.contains("Финиш") -> Color(0xFFFFC107)
        message.contains("Событие") -> Color(0xFFFF6F00)
        message.contains("Бот") -> DiceQuestTheme.colors.TextSecondary
        else -> DiceQuestTheme.colors.TextPrimary
    }
}

private fun getNotificationIcon(title: String): Int {
    return when (title.lowercase()) {
        "бонус" -> R.drawable.bonus
        "штраф" -> R.drawable.penalty
        "защита" -> R.drawable.shield
        "событие" -> R.drawable.event
        "победа" -> R.drawable.first_player
        "поражение" -> R.drawable.cross
        else -> R.drawable.bonus
    }
}

@Composable
fun GameBoardScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    mainViewModel: MainViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.loadGame()
    }

    val state = viewModel.state
    val isMultiplayer = state.isMultiplayer


    LaunchedEffect(isMultiplayer) {
        if (isMultiplayer) {
            while (true) {
                delay(2000L)
                viewModel.syncGameState()
            }
        }
    }

    val myPlayerId = UserRepository.PlayerId

    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFFA8E6CF)
    )

    val playerColors = remember(state.players) {
        state.players
            .mapIndexed { index, player ->
                player.id to colors[index % colors.size]
            }
            .toMap()
    }

    val board = remember(state.cells, state.players, playerColors) {
        buildBoard(state.cells, state.players, playerColors)
    }

    val showNotification = state.showNotification
    val notificationTitle = state.notificationTitle
    val notificationName = state.notificationName
    val notificationValue = state.notificationValue

    Box(modifier = Modifier.fillMaxSize()) {

        WithBottomNav(navController, BottomNavItem.Home) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                SpacerH(10)

                Header(
                    text = "Игровое поле",
                    leadingIcon = painterResource(R.drawable.arrow_left),
                    trailingIcon = null,
                    leadingOnClick = { navController.popBackStack() },
                    {}
                )

                SpacerH(8)

                // ===== ВЕРХНЯЯ ПАНЕЛЬ =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentPlayerColor = state.currentPlayer?.let { playerColors[it.id] } ?: Color.Gray
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(currentPlayerColor, shape = CircleShape)
                                .border(1.5.dp, DiceQuestTheme.colors.TextSecondary.copy(alpha = 0.3f), shape = CircleShape)
                        )
                        Text(
                            text = state.currentPlayer?.let {
                                if (it.isBot) "Бот" else "Игрок"
                            } ?: "—",
                            style = DiceQuestTheme.typography.titleMedium,
                            color = DiceQuestTheme.colors.TextPrimary
                        )
                    }
                }


                SpacerH(8)

                // ===== ИГРОВОЕ ПОЛЕ =====
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            DiceQuestTheme.colors.SurfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(board) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { cell ->
                                    if (cell == null) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        )
                                    } else {
                                        GameFieldCell(
                                            cell = cell,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SpacerH(8)

                // ===== КУБИК + ЛОГ =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 🔥 КУБИК — активен только если ход ТВОЕГО игрока
                        val isMyTurn = state.currentPlayer?.id == UserRepository.PlayerId && state.game?.status == "playing"

                        Dice(
                            modifier = Modifier.size(80.dp),
                            isEnabled = isMyTurn && state.canRollDice && !state.isTurnProcessing,
                            onRollComplete = { diceValue ->
                                val playerId = state.currentPlayer?.id
                                if (playerId != null && playerId == UserRepository.PlayerId) {
                                    viewModel.makeTurn(playerId, diceValue)
                                }
                            }
                        )

                        if (state.isTurnProcessing) {
                            Text(
                                text = "Загрузка...",
                                style = DiceQuestTheme.typography.bodySmall,
                                color = DiceQuestTheme.colors.Primary
                            )
                        }

                        // Цвет текущего игрока
                        val currentPlayerColor = state.currentPlayer?.let { playerColors[it.id] } ?: Color.Gray
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(currentPlayerColor, shape = CircleShape)
                                .border(1.5.dp, DiceQuestTheme.colors.TextSecondary.copy(alpha = 0.3f), shape = CircleShape)
                        )

                        // 🔥 Показываем, чей ход
                        Text(
                            text = if (isMyTurn) "Твой ход!" else "Ход: ${state.currentPlayer?.let { if (it.isBot) "Бот" else "Игрок" } ?: "—"}",
                            style = DiceQuestTheme.typography.bodySmall,
                            color = if (isMyTurn) DiceQuestTheme.colors.Success else DiceQuestTheme.colors.TextSecondary
                        )
                    }

                    // ===== ЛОГ =====
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                DiceQuestTheme.colors.SurfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(6.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Лог игры",
                                style = DiceQuestTheme.typography.labelSmall,
                                color = DiceQuestTheme.colors.TextSecondary
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                reverseLayout = true
                            ) {
                                items(state.gameLog.reversed()) { logMessage ->
                                    Text(
                                        text = logMessage,
                                        style = DiceQuestTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = getLogColor(logMessage),
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                SpacerH(16)
            }
        }

        // ===== УВЕДОМЛЕНИЕ =====
        if (showNotification) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        viewModel.dismissNotification()
                    },
                contentAlignment = Alignment.Center
            ) {
                CardEvents(
                    text = notificationTitle,
                    nameEvent = notificationName,
                    textIventPreview = notificationValue.ifEmpty { null },
                    imageIventPreview = painterResource(getNotificationIcon(notificationTitle))
                )
            }
        }
    }
}

private fun buildBoard(
    cells: List<Cell>,
    players: List<Player>,
    playerColors: Map<String, Color>
): List<List<GameCell?>> {
    if (cells.isEmpty()) return emptyList()

    val playersByPosition = players.groupBy { it.position }

    val rows = cells.groupBy { it.row }.toSortedMap()

    return rows.values.map { rowCells ->
        val boardRow = MutableList<GameCell?>(COLS) { null }

        rowCells.forEach { cell ->
            val tokens = playersByPosition[cell.number]
                ?.mapNotNull { playerColors[it.id] }
                ?: emptyList()

            if (cell.col in 0 until COLS) {
                boardRow[cell.col] = GameCell(
                    index = cell.number,
                    color = cell.cellColor(),
                    tokens = tokens
                )
            }
        }

        boardRow
    }
}