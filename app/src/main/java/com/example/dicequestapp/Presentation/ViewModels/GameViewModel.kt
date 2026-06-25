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

class GameViewModel(private val useCase: UseCase) : ViewModel() {

    private val _state = mutableStateOf(GameState())
    val state: GameState get() = _state.value

    fun updateState(newState: GameState) {
        _state.value = newState
    }

    /**
     * Основной метод: создание игры, игрока и генерация поля
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

                // ШАГ 2: Генерируем поле
                val cells = generateBoard()
                if (cells.isEmpty()) {
                    updateState(state.copy(
                        isLoading = false,
                        error = "Не удалось сгенерировать поле"
                    ))
                    return@launch
                }

                // ШАГ 3: Создаем игру
                val game = createGame(gameName, countPlayers, isMultiplayer, player.id)
                if (game == null) {
                    updateState(state.copy(
                        isLoading = false,
                        error = "Не удалось создать игру"
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
                    canStart = countPlayers == 1 // Для одиночной игры сразу можно стартовать
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
     * ШАГ 2: Генерация поля
     */
    private suspend fun generateBoard(): List<Cell> {
        val countCells = 35
        val cellsToCreate = createBoardCells(countCells)
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

        // Сортируем и сохраняем статистику
        val sortedCells = createdCells.sortedBy { it.number }
        updateCellStatistics(sortedCells)

        return sortedCells
    }

    /**
     * Создание списка ячеек для генерации
     */
    private fun createBoardCells(count: Int): List<CreateCell> {
        val cells = mutableListOf<CreateCell>()

        // 1. START (номер 0)
        cells.add(CreateCell(
            gameId = "", // Будет заполнено после создания игры
            number = 0,
            type = "start",
            value = ""
        ))

        // 2. FINISH (номер count - 1)
        cells.add(CreateCell(
            gameId = "",
            number = count - 1,
            type = "finish",
            value = ""
        ))

        // 3. Промежуточные ячейки (1 до count-2)
        val remainingCount = count - 2
        val typesDistribution = createTypeDistribution(remainingCount)

        var cellIndex = 1
        for (type in typesDistribution) {
            val (cellType, value) = createCellWithValue(type)
            cells.add(CreateCell(
                gameId = "",
                number = cellIndex,
                type = cellType,
                value = value
            ))
            cellIndex++
        }

        // 4. Перемешиваем промежуточные ячейки
        val intermediateCells = cells.subList(1, cells.size - 1)
        val shuffled = intermediateCells.shuffled()

        // 5. Пересобираем список с правильными номерами
        val result = mutableListOf(cells.first()) // START
        shuffled.forEachIndexed { index, cell ->
            result.add(cell.copy(number = index + 1))
        }
        result.add(cells.last().copy(number = count - 1)) // FINISH

        return result
    }

    /**
     * Распределение типов для промежуточных ячеек
     */
    private fun createTypeDistribution(count: Int): List<String> {
        val types = mutableListOf<String>()

        val bonusCount = (count * 0.23).toInt()
        val penaltyCount = (count * 0.23).toInt()
        val protectionCount = (count * 0.17).toInt()
        val eventCount = (count * 0.17).toInt()
        val emptyCount = count - (bonusCount + penaltyCount + protectionCount + eventCount)

        repeat(bonusCount) { types.add("bonus") }
        repeat(penaltyCount) { types.add("penalty") }
        repeat(protectionCount) { types.add("protection") }
        repeat(eventCount) { types.add("event") }
        repeat(emptyCount) { types.add("empty") }

        return types
    }

    /**
     * Создание ячейки с конкретным значением
     */
    private fun createCellWithValue(type: String): Pair<String, String> {
        return when (type) {
            "bonus" -> {
                val values = listOf("+3", "+5", "+7", "+9")
                Pair(type, values.random())
            }
            "penalty" -> {
                val values = listOf("-3", "-5", "-7", "-9")
                Pair(type, values.random())
            }
            "protection" -> {
                Pair(type, "shield")
            }
            "event" -> {
                val values = listOf("+3", "+5", "+7", "+9", "-3", "-5", "-7", "-9")
                Pair(type, values.random())
            }
            else -> Pair("empty", "")
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
     * ШАГ 3: Создание игры
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