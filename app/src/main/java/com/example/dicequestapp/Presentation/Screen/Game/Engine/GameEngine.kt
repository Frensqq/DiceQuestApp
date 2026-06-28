package com.example.dicequestapp.Presentation.Screen.Game.Engine

import android.util.Log
import com.example.dicequestapp.Presentation.Screen.Game.Engine.Repository.IGameRepository
import com.example.dicequestapp.Presentation.State.GameState
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Player.Player
import kotlinx.coroutines.delay
import kotlin.random.Random

class GameEngine(
    private val repository: IGameRepository,
    private val isHost: Boolean = false
) {

    private var game: Game? = null
    private var players: List<Player> = emptyList()
    private var cells: List<Cell> = emptyList()
    private var currentPlayerIndex: Int = 0
    private var cellsByNumber: Map<Int, Cell> = emptyMap()
    private val gameLog = mutableListOf<String>()

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
            }
            "protection" -> {
                players = players.map {
                    if (it.id == player.id) it.copy(shield = true) else it
                }
                addLog("$displayName (Защита) получил защиту")
            }
            "event" -> {
                if (player.shield) {
                    players = players.map {
                        if (it.id == player.id) it.copy(shield = false) else it
                    }
                    addLog("$displayName (Защита) сработала")
                    return
                }
                val eventValue = listOf("+3", "+5", "-3", "-5").random().toIntOrNull() ?: 0
                val newPos = (player.position + eventValue).coerceIn(1, maxCellNumber)
                players = players.map {
                    if (it.id == player.id) it.copy(position = newPos) else it
                }
                val type = if (eventValue > 0) "Бонус" else "Штраф"
                addLog("$displayName (Событие) $type $eventValue")
            }
            "finish" -> {
                players = players.map {
                    if (it.id == player.id) it.copy(finished = true) else it
                }
                addLog("$displayName (Финиш) финишировал")
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
            return
        }

        nextTurn()
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

    fun getState(): GameState {
        val currentPlayer = players.getOrNull(currentPlayerIndex)
        val isMyTurn = currentPlayer?.id == game?.currentPlayer

        return GameState(
            game = game,
            players = players,
            currentPlayer = currentPlayer,
            cells = cells,
            isMyTurn = isMyTurn,
            isHost = isHost,
            diceValue = game?.diceValue?.toInt() ?: 0,
            canRollDice = isMyTurn && game?.status == "playing",
            winner = players.find { it.finished },
            gameId = game?.id ?: "",
            gameLog = gameLog.toList()
        )
    }
}