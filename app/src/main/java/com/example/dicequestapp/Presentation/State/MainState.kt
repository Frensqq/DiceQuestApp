package com.example.dicequestapp.Presentation.State

import com.example.dq_net_library.Domain.Model.Game.Game
import com.example.dq_net_library.Domain.Model.Player.Player
import com.example.dq_net_library.Domain.Model.User.User

data class MainState (

    var isLoading:Boolean = false,
    var isSuccess:Boolean =false,
    var error: String?=null,
    var generalError: String? = null,
    var isNotInternet:Boolean = false,

    var userId: String = "",
    var username: String = "",

    var User: User? = null,

    var gameId: String = "",
    var multiplayer: Boolean = false,
    var nameGame: String = "",
    var countPlayer: Int = 2,
    var typeGame: String = "",
    var statusGame: String = "",

    var statePlayer: String = "",
    var BOT: Boolean = false,
    var positionPlayer: String = "",
    var Bonus: String = "",
    var Event: String = "",

    var MyPlayer: Player? = null,
    var Game: Game? = null,
    )