package com.example.dicequestapp.Presentation.Screen.Game.Engine

import android.util.Log
import com.example.dicequestapp.Presentation.Screen.Game.Component.GameNotification
import com.example.dicequestapp.Presentation.Screen.Game.Engine.Repository.IGameRepository
import com.example.dicequestapp.Presentation.State.GameState
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Player.Player
import kotlinx.coroutines.delay
import kotlin.random.Random

class GameEngine(
    private var repository: IGameRepository,
    private val isHost: Boolean = false
) {

    private var game: Game? = null
    private var players: List<Player> = emptyList()
    private var cells: List<Cell> = emptyList()
    private var currentPlayerIndex: Int = 0
    private var cellsByNumber: Map<Int, Cell> = emptyMap()
    private val gameLog = mutableListOf<String>()

    private var notification: GameNotification? = null

    private fun addLog(message: String) {
        gameLog.add(message)
        Log.d("GameEngine", message)
    }

    suspend fun initGame(gameId: String) {
        game = repository.getGame(gameId).getOrThrow()
        players = repository.getPlayers(gameId).getOrThrow()
        cells = repository.getCells(gameId).getOrThrow()

        cellsByNumber = cells.associateBy { it.number }

        players = players.map {
            if (it.position == 0) it.copy(position = 1) else it
        }

        currentPlayerIndex = players.indexOfFirst { it.id == game?.currentPlayer }
    }

    private fun getCellByNumber(number: Int): Cell? = cellsByNumber[number]
    private val maxCellNumber: Int get() = cellsByNumber.keys.maxOrNull() ?: 70

    private fun getColorName(player: Player): String {
        val index = players.indexOf(player)
        val colorNames = listOf("Красный", "Бирюзовый", "Жёлтый", "Мятный")
        return colorNames[index % colorNames.size]
    }

    private fun getPlayerDisplayName(player: Player): String {
        return if (player.isBot) {
            "${getColorName(player)} Бот ${players.indexOf(player) + 1}"
        } else {
            "Игрок"
        }
    }

    private fun movePlayer(player: Player, diceValue: Int): Player {
        var newPosition = player.position + diceValue
        if (newPosition > maxCellNumber) {
            newPosition = maxCellNumber
        }

        val updatedPlayer = player.copy(position = newPosition)
        players = players.map { if (it.id == player.id) updatedPlayer else it }

        return updatedPlayer
    }

    private suspend fun applyCell(player: Player, position: Int) {
        val cell = getCellByNumber(position) ?: return
        val displayName = getPlayerDisplayName(player)

        when (cell.type) {
            "bonus" -> {
                val bonus = cell.value.toIntOrNull() ?: 0
                val newPos = minOf(player.position + bonus, maxCellNumber)
                players = players.map {
                    if (it.id == player.id) it.copy(position = newPos) else it
                }
                addLog("$displayName (Бонус) +$bonus")

                if (!player.isBot) {
                    notify(
                        title = "Бонус",
                        text = "Вы получили бонус",
                        value = "+$bonus"
                    )
                }
            }
            "penalty" -> {
                if (player.shield) {
                    players = players.map {
                        if (it.id == player.id) it.copy(shield = false) else it
                    }
                    addLog("$displayName (Защита) сработала")
                    return
                }
                val penalty = cell.value.toIntOrNull() ?: 0
                val newPos = maxOf(player.position + penalty, 1)
                players = players.map {
                    if (it.id == player.id) it.copy(position = newPos) else it
                }
                addLog("$displayName (Штраф) $penalty")

                if (!player.isBot) {
                    notify(
                        title = "Штраф",
                        text = "Вы потеряли ходы",
                        value = "$penalty"
                    )
                }
            }
            "protection" -> {
                players = players.map {
                    if (it.id == player.id) it.copy(shield = true) else it
                }
                addLog("$displayName (Защита) получил защиту")

                if (!player.isBot) {
                    notify(
                        title = "Защита",
                        text = "Вы получили щит"
                    )
                }
            }
            "event" -> {
                if (player.shield) {
                    players = players.map {
                        if (it.id == player.id) it.copy(shield = false) else it
                    }
                    addLog("$displayName (Защита) сработала")
                    if (!player.isBot) {
                        notify(
                            title = "Защита",
                            text = "Щит защитил вас"
                        )
                    }
                    return
                }
                val eventValue = listOf("+3", "+5", "-3", "-5").random().toIntOrNull() ?: 0
                val newPos = (player.position + eventValue).coerceIn(1, maxCellNumber)
                players = players.map {
                    if (it.id == player.id) it.copy(position = newPos) else it
                }
                val type = if (eventValue > 0) "Бонус" else "Штраф"
                addLog("$displayName (Событие) $type $eventValue")
                if (!player.isBot) {
                    notify(
                        title = "Событие",
                        text = if (eventValue > 0)
                            "Вы продвинулись вперед"
                        else
                            "Вы откатились назад",
                        value = eventValue.toString()
                    )
                }
            }
            "finish" -> {
                players = players.map {
                    if (it.id == player.id) it.copy(finished = true) else it
                }
                addLog("$displayName (Финиш) финишировал")
                if (!player.isBot) {
                    notify(
                        title = "Победа",
                        text = "Поздравляем!"
                    )
                }
            }
        }

        players.forEach { repository.updatePlayer(it) }
    }

    private fun checkWin(player: Player): Boolean {
        val cell = getCellByNumber(player.position)
        return cell?.type == "finish"
    }

    fun rollDice(): Int {
        val value = Random.nextInt(1, 7)
        game = game?.copy(diceValue = value.toDouble())
        return value
    }


    suspend fun makeTurn(playerId: String, diceValue: Int) {
        val player = players.find { it.id == playerId } ?: return
        if (game?.currentPlayer != playerId) return

        val displayName = getPlayerDisplayName(player)
        addLog("$displayName бросил кубик: $diceValue")

        game = game?.copy(diceValue = diceValue.toDouble())
        repository.updateGame(game!!)

        val updatedPlayer = movePlayer(player, diceValue)
        addLog("$displayName перешёл на клетку ${updatedPlayer.position}")

        applyCell(updatedPlayer, updatedPlayer.position)

        if (checkWin(updatedPlayer)) {
            addLog("$displayName победил")
            game = game?.copy(status = "finished")
            repository.updateGame(game!!)
            // Сохраняем победителя в отдельную переменную
            winner = updatedPlayer
            return
        }

        nextTurn()
    }


    private var winner: Player? = null

    fun getState(): GameState {
        val currentPlayer = players.getOrNull(currentPlayerIndex)
        val isMyTurn = currentPlayer?.id == game?.currentPlayer

        // Находим игрока-человека (не бота)
        val humanPlayer = players.find { !it.isBot }

        // Проверяем, является ли humanPlayer создателем
        val isCreator = game?.creator == humanPlayer?.id

        return GameState(
            game = game,
            players = players,
            player = humanPlayer,
            playerId = humanPlayer?.id ?: "",
            currentPlayer = currentPlayer,
            cells = cells,
            isMyTurn = isMyTurn,
            canRollDice = isMyTurn && game?.status == "playing",
            isMultiplayer = game?.multiplayer ?: false,
            isHost = isHost,
            diceValue = game?.diceValue?.toInt() ?: 0,
            winner = winner,
            isGameFinished = game?.status == "finished",
            gameId = game?.id ?: "",
            gameLog = gameLog.toList(),
            isCreator = isCreator

        )
    }

    private suspend fun nextTurn() {
        var nextIndex = (currentPlayerIndex + 1) % players.size
        var attempts = 0

        while (players[nextIndex].finished && attempts < players.size) {
            nextIndex = (nextIndex + 1) % players.size
            attempts++
        }

        currentPlayerIndex = nextIndex
        val nextPlayer = players[nextIndex]

        game = game?.copy(
            currentPlayer = nextPlayer.id,
            currentTurn = (game?.currentTurn ?: 0.0) + 1.0
        )
        repository.updateGame(game!!)

        if (nextPlayer.isBot && isHost) {
            processBotTurn(nextPlayer)
        }
    }

    private suspend fun processBotTurn(bot: Player) {
        delay(1000)
        val diceValue = Random.nextInt(1, 7)
        makeTurn(bot.id, diceValue)
    }

    suspend fun resetGame() {
        players = players.map {
            it.copy(
                position = 1,
                shield = false,
                finished = false
            )
        }
        game = game?.copy(
            currentPlayer = players.firstOrNull()?.id ?: "",
            status = "playing",
            currentTurn = 0.0,
            diceValue = 0.0
        )

        players.forEach { repository.updatePlayer(it) }
        repository.updateGame(game!!)
        gameLog.clear()
    }

    private fun notify(
        title: String,
        text: String,
        value: String = ""
    ) {
        notification = GameNotification(title, text, value)
    }

    fun consumeNotification(): GameNotification? {
        val result = notification
        notification = null
        return result
    }

    suspend fun deleteGameData() {
        try {
            val gameId = game?.id ?: return

            Log.d("GameEngine", "=== Удаление клеток и игроков ===")
            Log.d("GameEngine", "Игроков для удаления: ${players.size}")
            Log.d("GameEngine", "Клеток для удаления: ${cells.size}")

            // 1. Удаляем всех игроков
            for (player in players) {
                Log.d("GameEngine", "Удаляем игрока: ${player.id}")
                repository.deletePlayer(player.id)
                Log.d("GameEngine", "Игрок удалён: ${player.id}")
            }

            // 2. Удаляем все клетки
            for (cell in cells) {
                Log.d("GameEngine", "Удаляем клетку: ${cell.id}")
                repository.deleteCell(cell.id)
                Log.d("GameEngine", "Клетка удалена: ${cell.id}")
            }

            // 3. Очищаем локальные данные
            players = emptyList()
            cells = emptyList()
            cellsByNumber = emptyMap()
            gameLog.clear()
            winner = null

            Log.d("GameEngine", "=== Все клетки и игроки удалены ===")

        } catch (e: Exception) {
            Log.e("GameEngine", "Ошибка при удалении данных: ${e.message}", e)
        }
    }

    /**
     * Удаление игрока по ID
     */
    suspend fun deletePlayer(playerId: String) {
        try {
            repository.deletePlayer(playerId)
            players = players.filter { it.id != playerId }
            Log.d("GameEngine", "Игрок удалён: $playerId")
        } catch (e: Exception) {
            Log.e("GameEngine", "Ошибка при удалении игрока: ${e.message}", e)
        }
    }
}