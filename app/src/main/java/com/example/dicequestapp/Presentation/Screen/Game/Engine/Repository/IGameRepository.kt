package com.example.dicequestapp.Presentation.Screen.Game.Engine.Repository

import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Player.Player

interface IGameRepository {
    suspend fun getGame(gameId: String): Result<Game>
    suspend fun updateGame(game: Game): Result<Game>
    suspend fun getPlayers(gameId: String): Result<List<Player>>
    suspend fun updatePlayer(player: Player): Result<Player>
    suspend fun getCells(gameId: String): Result<List<Cell>>
    suspend fun updateCell(cell: Cell): Result<Cell>
    suspend fun addPlayerToGame(gameId: String, player: Player): Result<Game>

    // Для онлайн режима
    suspend fun subscribeToGame(gameId: String, callback: (Game) -> Unit)
    suspend fun unsubscribeFromGame(gameId: String)
}