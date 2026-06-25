package com.example.dicequestapp.Presentation.State

import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Player.Player

data class GameState (
    var game: Game? = null,
    var gameId: String = "",

    // Информация об игроке
    var player: Player? = null,
    var playerId: String = "",

    // Ячейки поля
    var cells: List<Cell> = emptyList(),
    var isBoardGenerated: Boolean = false,

    // Статусы
    var isLoading: Boolean = false,
    var error: String? = null,

    // Статистика по ячейкам
    var bonusCount: Int = 0,
    var penaltyCount: Int = 0,
    var protectionCount: Int = 0,
    var eventCount: Int = 0,
    var emptyCount: Int = 0,

    // Для мультиплеера
    var isMultiplayer: Boolean = false,
    var expectedPlayers: Int = 2,
    var currentPlayers: Int = 0,

    // Состояние игры
    var canStart: Boolean = false,
    var gameStarted: Boolean = false
)