package com.example.dicequestapp.Presentation.Screen.Game.Component


import androidx.compose.ui.graphics.Color
import com.example.dq_net_library.Domain.Model.Player.Player

data class GamePlayer(
    val player: Player,
    val userName: String,
    val avatarUrl: String?,
    val isBot: Boolean,
    val position: Int = 0,
    val color: Color
)