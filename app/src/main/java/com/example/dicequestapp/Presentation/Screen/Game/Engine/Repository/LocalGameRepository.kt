package com.example.dicequestapp.Presentation.Screen.Game.Engine.Repository

import android.util.Log
import com.example.dicequestapp.Domain.UseCase
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Game.RedactGame
import com.example.dq_net_library.Domain.Model.NetworkResult
import com.example.dq_net_library.Domain.Model.Player.Player
import com.example.dq_net_library.Domain.Model.Player.RedactPlayer

class PocketBaseGameRepository(
    private val useCase: UseCase
) : IGameRepository {

    override suspend fun getGame(gameId: String): Result<Game> {
        return when (val result = useCase.getGame(gameId)) {
            is NetworkResult.Success -> Result.success(result.data)
            is NetworkResult.Error -> Result.failure(Exception(result.errorResponse.message))
            is NetworkResult.NoInternet -> Result.failure(Exception("No internet connection"))
        }
    }

    override suspend fun updateGame(game: Game): Result<Game> {
        return try {
            val request = RedactGame(
                players = game.players,
                type = game.type,
                status = game.status,
                start = game.start,
                end = game.end,
                countCell = game.countCell,
                currentPlayer = game.currentPlayer,
                currentTurn = game.currentTurn,
                diceValue = game.diceValue
            )
            when (val result = useCase.patchGames(game.id, request)) {
                is NetworkResult.Success -> Result.success(result.data)
                is NetworkResult.Error -> Result.failure(Exception(result.errorResponse.message))
                is NetworkResult.NoInternet -> Result.failure(Exception("No internet connection"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlayers(gameId: String): Result<List<Player>> {
        // 1. Сначала получаем игру
        val gameResult = getGame(gameId)
        if (gameResult.isFailure) {
            return Result.failure(gameResult.exceptionOrNull() ?: Exception("Game not found"))
        }

        val game = gameResult.getOrThrow()
        val playerIds = game.players
        Log.d("PocketBaseRepo", "getPlayers: found ${playerIds.size} player IDs in game")

        if (playerIds.isEmpty()) {
            return Result.success(emptyList())
        }

        // 2. Загружаем каждого игрока по ID
        val players = mutableListOf<Player>()
        for (playerId in playerIds) {
            when (val result = useCase.getPlayer(playerId)) {
                is NetworkResult.Success -> {
                    players.add(result.data)
                }
                is NetworkResult.Error -> {
                    Log.e("PocketBaseRepo", "Error loading player $playerId: ${result.errorResponse.message}")
                    return Result.failure(Exception("Failed to load player: ${result.errorResponse.message}"))
                }
                is NetworkResult.NoInternet -> {
                    return Result.failure(Exception("No internet connection"))
                }
            }
        }

        Log.d("PocketBaseRepo", "getPlayers: loaded ${players.size} players")
        return Result.success(players)
    }

    override suspend fun updatePlayer(player: Player): Result<Player> {
        return try {
            val request = RedactPlayer(
                position = player.position,
                shield = player.shield,
                finished = player.finished
            )
            when (val result = useCase.patchPlayer(player.id, request)) {
                is NetworkResult.Success -> Result.success(result.data)
                is NetworkResult.Error -> Result.failure(Exception(result.errorResponse.message))
                is NetworkResult.NoInternet -> Result.failure(Exception("No internet connection"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCells(gameId: String): Result<List<Cell>> {
        Log.d("PocketBaseRepo", "=== getCells START ===")
        Log.d("PocketBaseRepo", "gameId: $gameId")

        val filter = "gameId='$gameId'"
        val allCells = mutableListOf<Cell>()
        var page = 1
        val perPage = 100

        while (true) {
            Log.d("PocketBaseRepo", "Loading page $page with perPage=$perPage")
            val result = useCase.getCells(filter, page, perPage)

            when (result) {
                is NetworkResult.Success -> {
                    val items = result.data.items
                    Log.d("PocketBaseRepo", "Page $page loaded: ${items.size} items")
                    allCells.addAll(items)

                    val totalPages = result.data.totalPages
                    if (page >= totalPages) {
                        break
                    }
                    page++
                }
                is NetworkResult.Error -> {
                    Log.e("PocketBaseRepo", "Error loading cells: ${result.errorResponse.message}")
                    return Result.failure(Exception(result.errorResponse.message))
                }
                is NetworkResult.NoInternet -> {
                    return Result.failure(Exception("No internet connection"))
                }
            }
        }

        Log.d("PocketBaseRepo", "Total cells loaded: ${allCells.size}")
        return Result.success(allCells)
    }

    override suspend fun updateCell(cell: Cell): Result<Cell> {
        return Result.success(cell)
    }

    override suspend fun addPlayerToGame(gameId: String, player: Player): Result<Game> {
        return try {
            val currentGame = getGame(gameId).getOrThrow()
            val updatedPlayers = currentGame.players + player.id
            val request = RedactGame(
                players = updatedPlayers,
                type = currentGame.type,
                status = currentGame.status,
                start = currentGame.start,
                end = currentGame.end,
                countCell = currentGame.countCell,
                currentPlayer = currentGame.currentPlayer,
                currentTurn = currentGame.currentTurn,
                diceValue = currentGame.diceValue
            )
            when (val result = useCase.patchGames(gameId, request)) {
                is NetworkResult.Success -> Result.success(result.data)
                is NetworkResult.Error -> Result.failure(Exception(result.errorResponse.message))
                is NetworkResult.NoInternet -> Result.failure(Exception("No internet connection"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun subscribeToGame(gameId: String, callback: (Game) -> Unit) {
        // Для одиночной игры не нужно
    }

    override suspend fun unsubscribeFromGame(gameId: String) {
        // Для одиночной игры не нужно
    }
}