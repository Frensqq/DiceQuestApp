package com.example.dicequestapp.Presentation.Screen.Game.Engine

import android.util.Log
import com.example.dicequestapp.Presentation.Screen.Game.Engine.Repository.IGameRepository
import com.example.dicequestapp.Presentation.State.GameState
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Player.Player
import kotlinx.coroutines.delay
import kotlin.collections.find
import kotlin.collections.firstOrNull
import kotlin.collections.getOrNull
import kotlin.collections.map
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

    suspend fun initGame(gameId: String) {
        game = repository.getGame(gameId).getOrThrow()
        players = repository.getPlayers(gameId).getOrThrow()
        cells = repository.getCells(gameId).getOrThrow()

        // Строим карту клеток по номеру для быстрого доступа
        cellsByNumber = cells.associateBy { it.number }

        // Все игроки должны начинать с клетки номер 1
        players = players.map {
            if (it.position == 0) it.copy(position = 1) else it
        }

        currentPlayerIndex = players.indexOfFirst { it.id == game?.currentPlayer }
    }

    private fun getCellByNumber(number: Int): Cell? = cellsByNumber[number]

    private fun movePlayer(player: Player, diceValue: Int): Player {
        var newPosition = player.position + diceValue
        val maxPosition = cellsByNumber.keys.maxOrNull() ?: 70

        if (newPosition > maxPosition) {
            newPosition = maxPosition
        }

        val updatedPlayer = player.copy(position = newPosition)
        players = players.map { if (it.id == player.id) updatedPlayer else it }

        return updatedPlayer
    }

    private suspend fun applyCell(player: Player, position: Int) {
        val cell = getCellByNumber(position) ?: return

        when (cell.type) {
            "bonus" -> {
                val bonus = cell.value.toIntOrNull() ?: 0
                val newPos = minOf(player.position + bonus, cellsByNumber.keys.maxOrNull() ?: 70)
                players = players.map {
                    if (it.id == player.id) it.copy(position = newPos) else it
                }
            }
            "penalty" -> {
                if (player.shield) {
                    players = players.map {
                        if (it.id == player.id) it.copy(shield = false) else it
                    }
                    return
                }
                val penalty = cell.value.toIntOrNull() ?: 0
                val newPos = maxOf(player.position + penalty, 1) // Минимум клетка 1
                players = players.map {
                    if (it.id == player.id) it.copy(position = newPos) else it
                }
            }
            "protection" -> {
                players = players.map {
                    if (it.id == player.id) it.copy(shield = true) else it
                }
            }
            "event" -> {
                if (player.shield) {
                    players = players.map {
                        if (it.id == player.id) it.copy(shield = false) else it
                    }
                    return
                }
                val eventValue = listOf("+3", "+5", "-3", "-5").random().toIntOrNull() ?: 0
                val newPos = (player.position + eventValue).coerceIn(1, cellsByNumber.keys.maxOrNull() ?: 70)
                players = players.map {
                    if (it.id == player.id) it.copy(position = newPos) else it
                }
            }
            "finish" -> {
                players = players.map {
                    if (it.id == player.id) it.copy(finished = true) else it
                }
            }
        }

        players.forEach { repository.updatePlayer(it) }
    }

    private fun checkWin(player: Player): Boolean {
        val maxPosition = cellsByNumber.keys.maxOrNull() ?: 70
        return player.position >= maxPosition
    }



    fun rollDice(): Int {
        val value = Random.nextInt(1, 7)
        game = game?.copy(diceValue = value.toDouble())
        return value
    }


    suspend fun makeTurn(playerId: String, diceValue: Int) {
        val player = players.find { it.id == playerId } ?: return
        if (game?.currentPlayer != playerId) return

        game = game?.copy(diceValue = diceValue.toDouble())
        repository.updateGame(game!!)

        // 1. Перемещаем игрока и получаем обновленного
        val updatedPlayer = movePlayer(player, diceValue)

        // 2. Применяем эффект ячейки к обновленному игроку
        applyCell(updatedPlayer, updatedPlayer.position)

        if (checkWin(updatedPlayer)) {
            game = game?.copy(status = "finished")
            repository.updateGame(game!!)
            return
        }

        nextTurn()
    }



    private suspend fun nextTurn() {
        Log.d("GameEngine", "=== nextTurn START ===")
        Log.d("GameEngine", "currentPlayerIndex: $currentPlayerIndex")
        Log.d("GameEngine", "players.size: ${players.size}")

        var nextIndex = (currentPlayerIndex + 1) % players.size
        var attempts = 0

        while (players[nextIndex].finished && attempts < players.size) {
            Log.d("GameEngine", "Player ${players[nextIndex].id} is finished, skipping")
            nextIndex = (nextIndex + 1) % players.size
            attempts++
        }

        currentPlayerIndex = nextIndex
        val nextPlayer = players[nextIndex]
        Log.d("GameEngine", "Next player: ${nextPlayer.id}, isBot: ${nextPlayer.isBot}")

        game = game?.copy(
            currentPlayer = nextPlayer.id,
            currentTurn = (game?.currentTurn ?: 0.0) + 1.0
        )
        repository.updateGame(game!!)

        if (nextPlayer.isBot && isHost) {
            Log.d("GameEngine", "Next player is bot, processing bot turn")
            processBotTurn(nextPlayer)
        } else {
            Log.d("GameEngine", "Next player is human, waiting for input")
        }
        Log.d("GameEngine", "=== nextTurn END ===")
    }

    private suspend fun processBotTurn(bot: Player) {
        delay(1000)
        val diceValue = Random.nextInt(1, 7)
        makeTurn(bot.id, diceValue)
    }

    suspend fun resetGame() {
        players = players.map {
            it.copy(
                position = 0,
                shield = false,
                finished = false
            )
        }
        game = game?.copy(
            currentPlayer = players.firstOrNull()?.id ?: "",
            status = "playing",
            currentTurn = 0.00,
            diceValue = 0.00
        )

        players.forEach { repository.updatePlayer(it) }
        repository.updateGame(game!!)
    }

    fun getState(): GameState {
        val currentPlayer = players.getOrNull(currentPlayerIndex)
        val isMyTurn = currentPlayer?.id == game?.currentPlayer

        return GameState(
            game = game,
            players = players,  // актуальный список игроков
            currentPlayer = currentPlayer,
            cells = cells,
            isMyTurn = isMyTurn,
            isHost = isHost,
            diceValue = game?.diceValue?.toInt() ?: 0,
            canRollDice = isMyTurn && game?.status == "playing",
            winner = players.find { it.finished },
            gameId = game?.id ?: ""
        )
    }
}