package com.example.dicequestapp.Presentation.Screen.Game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.dicequestapp.Presentation.Navigation.WithBottomNav
import com.example.dicequestapp.Presentation.Screen.Game.Component.GamePlayer
import com.example.dicequestapp.Presentation.ViewModels.GameViewModel
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_ui.Button.ButtonBig
import com.example.dq_ui.Field.GameCell
import com.example.dq_ui.Field.GameFieldCell
import com.example.dq_ui.Headers.Header
import com.example.dq_ui.R
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.icons.BottomNavItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameBoardScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    mainViewModel: MainViewModel
) {
    val state = viewModel.state

    // Фильтруем ячейки по gameId
    val cellsForGame = remember(state.cells, state.gameId) {
        state.cells.filter { it.gameId == state.gameId }
    }

    val userState = mainViewModel.state
    val userAvatarUrl = userState.User?.let { user ->
        if (user.avatar.isNotEmpty()) {
            mainViewModel.getImageUrl("users", user.id, user.avatar)
        } else null
    }

    val players = remember(
        state.player,
        state.expectedPlayers,
        state.isMultiplayer,
        userState.User
    ) {
        buildPlayersList(
            state,
            userState.User?.userName ?: "Игрок",
            userAvatarUrl
        )
    }

    // Строим карту поля по координатам из отфильтрованных ячеек
    val board = remember(cellsForGame, players) {
        buildBoard(cellsForGame, players)
    }
    // Получаем все уникальные col из координат
    val maxCols = 7 // Фиксированное количество колонок

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
                leadingOnClick = {
                    navController.popBackStack()
                },
                {}
            )

            SpacerH(8)

            // Информация об игре
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "🎯 Ход: Игрок 1",
                    style = DiceQuestTheme.typography.titleMedium,
                    color = DiceQuestTheme.colors.TextPrimary
                )
                Text(
                    text = "🎲 Бросок: -",
                    style = DiceQuestTheme.typography.titleMedium,
                    color = DiceQuestTheme.colors.TextPrimary
                )
            }

            SpacerH(8)

            // Игровое поле - строго по координатам (row, col)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        DiceQuestTheme.colors.SurfaceVariant.copy(alpha = 0.3f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
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

            // Кнопка броска кубика
            ButtonBig(
                text = "🎲 Бросить кубик",
                onClick = {
                    // TODO: Логика броска кубика
                },
                type = true
            )

            SpacerH(16)
        }
    }
}

/**
 * Создание списка игроков на поле
 */
private fun buildPlayersList(
    state: com.example.dicequestapp.Presentation.State.GameState,
    userName: String,
    avatarUrl: String?
): List<GamePlayer> {
    val players = mutableListOf<GamePlayer>()

    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFFA8E6CF)
    )

    players.add(
        GamePlayer(
            player = state.player ?: return emptyList(),
            userName = userName,
            avatarUrl = avatarUrl,
            isBot = false,
            position = 0,
            color = colors[0]
        )
    )

    if (!state.isMultiplayer) {
        val botNames = listOf("Bot 1", "Bot 2", "Bot 3")
        for (i in 1 until state.expectedPlayers) {
            players.add(
                GamePlayer(
                    player = state.player ?: return emptyList(),
                    userName = botNames[i - 1],
                    avatarUrl = null,
                    isBot = true,
                    position = 0,
                    color = colors[i % colors.size]
                )
            )
        }
    } else {
        for (i in 1 until state.expectedPlayers) {
            players.add(
                GamePlayer(
                    player = state.player ?: return emptyList(),
                    userName = "Игрок ${i + 1}",
                    avatarUrl = null,
                    isBot = false,
                    position = 0,
                    color = colors[i % colors.size]
                )
            )
        }
    }

    return players
}


private const val COLS = 7

private fun buildBoard(
    cells: List<Cell>,
    players: List<GamePlayer>
): List<List<GameCell?>> {

    if (cells.isEmpty()) return emptyList()

    // Один раз строим карту "позиция -> игроки"
    val playersByPosition = players.groupBy { it.position }

    // Группируем клетки по строкам
    val rows = cells
        .groupBy { it.row }
        .toSortedMap()

    return rows.values.map { rowCells ->

        val boardRow = MutableList<GameCell?>(COLS) { null }

        rowCells.forEach { cell ->

            val tokens = playersByPosition[cell.number - 1]
                ?.map { it.color }
                ?: emptyList()

            val color = when (cell.type) {
                "start" -> DiceQuestTheme.colors.Primary.copy(alpha = 0.3f)
                "finish" -> DiceQuestTheme.colors.Primary.copy(alpha = 0.3f)
                "bonus" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                "penalty" -> Color(0xFFE53935).copy(alpha = 0.2f)
                "protection" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                "event" -> Color(0xFFFFC107).copy(alpha = 0.2f)
                else -> DiceQuestTheme.colors.Surface
            }

            if (cell.col in 0 until COLS) {
                boardRow[cell.col] = GameCell(
                    index = cell.number,
                    color = color,
                    tokens = tokens
                )
            }
        }

        boardRow
    }
}