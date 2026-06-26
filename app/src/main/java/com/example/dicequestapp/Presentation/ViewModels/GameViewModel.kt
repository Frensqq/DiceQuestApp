package com.example.dicequestapp.Presentation.ViewModels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicequestapp.Domain.UseCase
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.State.GameState
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Cell.CreateCell
import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Game.RequestCreateGame
import com.example.dq_net_library.Domain.Model.NetworkResult
import com.example.dq_net_library.Domain.Model.Player.CreatePlayer
import com.example.dq_net_library.Domain.Model.Player.Player
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(private val useCase: UseCase) : ViewModel() {

    private val _state = mutableStateOf(GameState())
    val state: GameState get() = _state.value

    fun updateState(newState: GameState) {
        _state.value = newState
    }

    /**
     * Основной метод: создание игры, игрока и генерация поля
     * ПРАВИЛЬНЫЙ ПОРЯДОК:
     * 1. Создаем Player
     * 2. Создаем Game (получаем gameId)
     * 3. Генерируем поле с правильным gameId
     */
    fun createGameAndBoard(
        gameName: String,
        countPlayers: Int,
        isMultiplayer: Boolean,
        onGameCreated: () -> Unit
    ) {
        viewModelScope.launch {
            updateState(state.copy(
                isLoading = true,
                error = null,
                isMultiplayer = isMultiplayer,
                expectedPlayers = countPlayers
            ))

            try {
                // ШАГ 1: Создаем Player
                val player = createPlayer()
                if (player == null) {
                    updateState(state.copy(
                        isLoading = false,
                        error = "Не удалось создать игрока"
                    ))
                    return@launch
                }

                // ШАГ 2: Создаем игру (получаем gameId)
                val game = createGame(gameName, countPlayers, isMultiplayer, player.id)
                if (game == null) {
                    updateState(state.copy(
                        isLoading = false,
                        error = "Не удалось создать игру"
                    ))
                    return@launch
                }

                // ШАГ 3: Генерируем поле с правильным gameId
                val cells = generateBoard(game.id)
                if (cells.isEmpty()) {
                    updateState(state.copy(
                        isLoading = false,
                        error = "Не удалось сгенерировать поле"
                    ))
                    return@launch
                }

                // Обновляем состояние
                updateState(state.copy(
                    game = game,
                    gameId = game.id,
                    player = player,
                    playerId = player.id,
                    cells = cells,
                    isBoardGenerated = true,
                    isLoading = false,
                    canStart = countPlayers == 1
                ))

                Log.d("GameViewModel", "Игра создана: ${game.id}")
                Log.d("GameViewModel", "Игрок создан: ${player.id}")
                Log.d("GameViewModel", "Сгенерировано ячеек: ${cells.size}")

                onGameCreated()

            } catch (e: Exception) {
                updateState(state.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка создания игры"
                ))
                Log.e("GameViewModel", "Ошибка: ${e.message}", e)
            }
        }
    }

    /**
     * ШАГ 1: Создание игрока
     */
    private suspend fun createPlayer(): Player? {
        return try {
            val request = CreatePlayer(
                userId = UserRepository.UserId,
                BOT = false,
                state = "waiting",
                position = "0",
                protect = false,
                Bonus = "",
                Event = ""
            )

            when (val response = useCase.createPlayer(request)) {
                is NetworkResult.Success -> {
                    Log.d("GameViewModel", "Player created: ${response.data.id}")
                    UserRepository.PlayerId = response.data.id
                    response.data
                }
                is NetworkResult.Error -> {
                    Log.e("GameViewModel", "Error creating player: ${response.errorResponse.message}")
                    null
                }
                is NetworkResult.NoInternet -> {
                    Log.e("GameViewModel", "No internet connection")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "Exception creating player: ${e.message}", e)
            null
        }
    }

    /**
     * ШАГ 2: Создание игры
     */
    private suspend fun createGame(
        name: String,
        countPlayers: Int,
        isMultiplayer: Boolean,
        playerId: String
    ): Game? {
        return try {
            val request = RequestCreateGame(
                name = name,
                players = listOf(playerId),
                countPlayer = countPlayers,
                multiplayer = isMultiplayer,
                type = if (isMultiplayer) "multiplayer" else "single",
                status = "waiting",
                creator = playerId,
                start = "",
                end = "",
                countCell = 35,
                currentPlayer = playerId
            )

            when (val response = useCase.createGame(request)) {
                is NetworkResult.Success -> {
                    Log.d("GameViewModel", "Game created: ${response.data.id}")
                    UserRepository.GameId = response.data.id
                    response.data
                }
                is NetworkResult.Error -> {
                    Log.e("GameViewModel", "Error creating game: ${response.errorResponse.message}")
                    null
                }
                is NetworkResult.NoInternet -> {
                    Log.e("GameViewModel", "No internet connection")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("GameViewModel", "Exception creating game: ${e.message}", e)
            null
        }
    }

    /**
     * ШАГ 3: Генерация поля с правильным gameId
     */
    private suspend fun generateBoard(gameId: String): List<Cell> {
        val countCells = 70
        val cellsToCreate = createBoardCells(gameId, countCells)
        val createdCells = mutableListOf<Cell>()

        for (cellRequest in cellsToCreate) {
            try {
                when (val response = useCase.createCell(cellRequest)) {
                    is NetworkResult.Success -> {
                        createdCells.add(response.data)
                        Log.d("GameViewModel", "Cell ${response.data.number} created: ${response.data.type}")
                    }
                    is NetworkResult.Error -> {
                        Log.e("GameViewModel", "Error creating cell: ${response.errorResponse.message}")
                        return emptyList()
                    }
                    is NetworkResult.NoInternet -> {
                        Log.e("GameViewModel", "No internet connection")
                        return emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Exception creating cell: ${e.message}", e)
                return emptyList()
            }
        }

        val sortedCells = createdCells.sortedBy { it.number }
        updateCellStatistics(sortedCells)

        return sortedCells
    }

    /**
     * Создание списка ячеек для генерации с правильным gameId
     */
    private fun createBoardCells(
        gameId: String,
        count: Int
    ): List<CreateCell> {

        val result = mutableListOf<CreateCell>()

        val width = 7

        var number = 1
        var row = 0
        var leftToRight = true

        while (number <= count) {

            // Горизонтальная линия
            val cols = if (leftToRight) {
                0 until width
            } else {
                (width - 1 downTo 0)
            }

            for (col in cols) {

                if (number > count) break

                val (type, value) = getCellTypeAndValue(number, count)

                result += CreateCell(
                    gameId = gameId,
                    number = number,
                    type = type,
                    value = value,
                    row = row,
                    col = col
                )

                number++
            }

            if (number > count) break

            // Вертикальная клетка
            row++

            val verticalCol = if (leftToRight) width - 1 else 0

            val (type, value) = getCellTypeAndValue(number, count)

            result += CreateCell(
                gameId = gameId,
                number = number,
                type = type,
                value = value,
                row = row,
                col = verticalCol
            )

            number++

            row++

            leftToRight = !leftToRight
        }

        return result
    }

    /**
     * Определение типа и значения ячейки
     */
    private fun getCellTypeAndValue(
        number: Int,
        totalCount: Int
    ): Pair<String, String> {

        if (number == 1) {
            return "start" to ""
        }

        if (number == totalCount) {
            return "finish" to ""
        }

        return when (Random.nextInt(100)) {

            in 0..19 -> // 20%
                "bonus" to listOf("+3", "+5", "+7").random()

            in 20..39 -> // 20%
                "penalty" to listOf("-3", "-5", "-7").random()

            in 40..49 -> // 10%
                "event" to listOf(
                    "+3",
                    "+5",
                    "-3",
                    "-5"
                ).random()

            in 50..54 -> // 5%
                "protection" to "shield"

            else -> // 45%
                "empty" to ""
        }
    }
    /**
     * Подсчет статистики по ячейкам
     */
    private fun updateCellStatistics(cells: List<Cell>) {
        var bonus = 0
        var penalty = 0
        var protection = 0
        var event = 0
        var empty = 0

        cells.forEach { cell ->
            when (cell.type) {
                "bonus" -> bonus++
                "penalty" -> penalty++
                "protection" -> protection++
                "event" -> event++
                "empty" -> empty++
            }
        }

        updateState(state.copy(
            bonusCount = bonus,
            penaltyCount = penalty,
            protectionCount = protection,
            eventCount = event,
            emptyCount = empty
        ))
    }

    /**
     * Получение цвета ячейки по типу
     */
    fun getCellColor(type: String): androidx.compose.ui.graphics.Color {
        return when (type) {
            "bonus" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            "penalty" -> androidx.compose.ui.graphics.Color(0xFFE53935)
            "protection" -> androidx.compose.ui.graphics.Color(0xFF2196F3)
            "event" -> androidx.compose.ui.graphics.Color(0xFFFFC107)
            "start" -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
            "finish" -> androidx.compose.ui.graphics.Color(0xFFFF6F00)
            else -> androidx.compose.ui.graphics.Color(0xFF757575)
        }
    }
}