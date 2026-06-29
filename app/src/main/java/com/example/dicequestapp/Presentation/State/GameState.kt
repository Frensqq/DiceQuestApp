package com.example.dicequestapp.Presentation.State

import androidx.compose.ui.graphics.painter.Painter
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Player.Player

data class GameState (
    var gameId: String = "",
    var player: Player? = null,
    var playerId: String = "",
    var isBoardGenerated: Boolean = false,
    var isLoading: Boolean = false,
    var error: String? = null,
    var bonusCount: Int = 0,
    var penaltyCount: Int = 0,
    var protectionCount: Int = 0,
    var eventCount: Int = 0,
    var emptyCount: Int = 0,
    var isMultiplayer: Boolean = false,
    var expectedPlayers: Int = 2,
    var currentPlayers: Int = 0,
    var canStart: Boolean = false,
    var gameStarted: Boolean = false,


    val game: Game? = null,
    val players: List<Player> = emptyList(),
    val currentPlayer: Player? = null,
    val cells: List<Cell> = emptyList(),
    val isMyTurn: Boolean = false,
    val isHost: Boolean = false,
    val diceValue: Int = 0,
    val canRollDice: Boolean = true,
    val winner: Player? = null,
    val gameLog: List<String> = emptyList(),
    val showNotification: Boolean = false,
    val notificationTitle: String = "",
    val notificationName: String = "",
    val notificationValue: String = "",
    val notificationImage: Painter? = null,
    val isWinner: Boolean = false,
    val isGameFinished: Boolean = false,
    val isTurnProcessing: Boolean = false,
    val isCreator: Boolean = false


)