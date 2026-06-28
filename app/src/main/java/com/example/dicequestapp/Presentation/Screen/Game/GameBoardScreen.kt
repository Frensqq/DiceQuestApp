package com.example.dicequestapp.Presentation.Screen.Game

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.navigation.NavHostController
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.WithBottomNav
import com.example.dicequestapp.Presentation.ViewModels.GameViewModel
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Player.Player
import com.example.dq_ui.Dice.Dice
import com.example.dq_ui.Dice.DiceResultDialog
import com.example.dq_ui.Field.GameCell
import com.example.dq_ui.Field.GameFieldCell
import com.example.dq_ui.Headers.Header
import com.example.dq_ui.R
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.icons.BottomNavItem

private const val COLS = 7

/**
 * Цвет ячейки по типу
 */
private fun Cell.cellColor(): Color = when (type) {
    "start" -> DiceQuestTheme.colors.Primary.copy(alpha = 0.3f)
    "finish" -> DiceQuestTheme.colors.Primary.copy(alpha = 0.3f)
    "bonus" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
    "penalty" -> Color(0xFFE53935).copy(alpha = 0.2f)
    "protection" -> Color(0xFF2196F3).copy(alpha = 0.2f)
    "event" -> Color(0xFFFFC107).copy(alpha = 0.2f)
    else -> DiceQuestTheme.colors.Surface
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

    // Цвета для игроков
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

    // Строим поле
    val board = remember(state.cells, state.players, playerColors) {
        buildBoard(state.cells, state.players, playerColors)
    }

    var showDiceResult by remember { mutableStateOf(false) }
    var lastDiceResult by remember { mutableStateOf(0) }

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

            // Информация об игре
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Ход: ${state.currentPlayer?.let { if (it.isBot) "Бот" else "Игрок" } ?: "—"}",
                    style = DiceQuestTheme.typography.titleMedium,
                    color = DiceQuestTheme.colors.TextPrimary
                )
                Text(
                    text = "Бросок: ${if (state.diceValue > 0) state.diceValue else "-"}",
                    style = DiceQuestTheme.typography.titleMedium,
                    color = DiceQuestTheme.colors.TextPrimary
                )
            }

            SpacerH(8)

            // Игровое поле
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

            // Кубик с цветным индикатором текущего игрока
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dice(
                    modifier = Modifier.size(80.dp),
                    isEnabled = state.canRollDice,
                    onResult = { diceValue ->
                        lastDiceResult = diceValue
                        showDiceResult = true

                        val playerId = state.currentPlayer?.id
                        if (playerId != null) {
                            viewModel.makeTurn(playerId, diceValue)
                        }
                    }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Цветной индикатор текущего игрока
                    val currentPlayerColor = state.currentPlayer?.let { playerColors[it.id] } ?: Color.Gray

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                currentPlayerColor,
                                shape = CircleShape
                            )
                            .border(
                                2.dp,
                                DiceQuestTheme.colors.TextSecondary.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )

                    Text(
                        text = if (state.isMyTurn) "Твой ход!" else "Ожидание...",
                        style = DiceQuestTheme.typography.titleMedium,
                        color = if (state.isMyTurn)
                            DiceQuestTheme.colors.Success
                        else
                            DiceQuestTheme.colors.TextSecondary
                    )
                    Text(
                        text = if (state.isMyTurn) "Брось кубик!" else "Ход другого игрока...",
                        style = DiceQuestTheme.typography.bodyMedium,
                        color = DiceQuestTheme.colors.TextSecondary
                    )
                }
            }

            SpacerH(16)
        }
    }

    if (showDiceResult) {
        DiceResultDialog(
            result = lastDiceResult,
            onDismiss = { showDiceResult = false }
        )
    }
}

/**
 * Построение игрового поля
 */
private fun buildBoard(
    cells: List<Cell>,
    players: List<Player>,
    playerColors: Map<String, Color>
): List<List<GameCell?>> {
    if (cells.isEmpty()) return emptyList()

    // Группируем игроков по позиции (position = номер клетки)
    val playersByPosition = players.groupBy { it.position }

    // Группируем клетки по строкам
    val rows = cells.groupBy { it.row }.toSortedMap()

    return rows.values.map { rowCells ->
        val boardRow = MutableList<GameCell?>(COLS) { null }

        rowCells.forEach { cell ->
            // Игроки на этой клетке (position == cell.number)
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