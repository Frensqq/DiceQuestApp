package com.example.dicequestapp.Presentation.ViewModels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Screen.Game.Engine.GameEngine
import com.example.dicequestapp.Presentation.State.GameState
import kotlinx.coroutines.launch

class GameViewModel(
    private val engineDi: GameEngine
) : ViewModel() {

    private val _state = mutableStateOf(GameState())
    val state: GameState get() = _state.value

    val engine: GameEngine = engineDi

    fun loadGame() {
        val gameId = UserRepository.GameId
        if (gameId.isEmpty()) {
            Log.e("GameViewModel", "GameId is empty")
            return
        }
        viewModelScope.launch {
            try {
                Log.d("GameViewModel", "loadGame: gameId=$gameId")
                engine.initGame(gameId)
                _state.value = engine.getState()
                Log.d("GameViewModel", "Game loaded, cells: ${_state.value.cells.size}")
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error loading game: ${e.message}", e)
                _state.value = state.copy(error = e.message)
            }
        }
    }

    fun updateState(newState: GameState) {
        _state.value = newState
    }

    fun makeTurn(playerId: String, diceValue: Int) {
        viewModelScope.launch {
            try {
                Log.d("GameViewModel", "makeTurn: playerId=$playerId, diceValue=$diceValue")

                // Запоминаем текущего игрока до хода
                val currentPlayer = _state.value.currentPlayer
                val isBot = currentPlayer?.isBot ?: false

                engine.makeTurn(playerId, diceValue)
                _state.value = engine.getState()

                val lastLog = _state.value.gameLog.lastOrNull()

                if (!isBot && lastLog != null) {
                    when {
                        lastLog.contains("Бонус +") -> {
                            val value = lastLog.substringAfter("Бонус +").takeWhile { it.isDigit() }.toIntOrNull() ?: 0
                            _state.value = _state.value.copy(
                                showNotification = true,
                                notificationTitle = "Бонус",
                                notificationName = "Вы получили бонус!",
                                notificationValue = "+$value"
                            )
                        }
                        lastLog.contains("Штраф -") -> {
                            val value = lastLog.substringAfter("Штраф -").takeWhile { it.isDigit() }.toIntOrNull() ?: 0
                            _state.value = _state.value.copy(
                                showNotification = true,
                                notificationTitle = "Штраф",
                                notificationName = "Вы получили штраф!",
                                notificationValue = "-$value"
                            )
                        }
                        lastLog.contains("Защита получил") -> {
                            _state.value = _state.value.copy(
                                showNotification = true,
                                notificationTitle = "Защита",
                                notificationName = "Вы защищены!",
                                notificationValue = "🛡"
                            )
                        }
                        lastLog.contains("Защита сработала") -> {
                            _state.value = _state.value.copy(
                                showNotification = true,
                                notificationTitle = "Защита",
                                notificationName = "Защита сработала!",
                                notificationValue = "🛡"
                            )
                        }
                        lastLog.contains("Событие") -> {
                            _state.value = _state.value.copy(
                                showNotification = true,
                                notificationTitle = "Событие",
                                notificationName = "Произошло событие!",
                                notificationValue = ""
                            )
                        }
                        lastLog.contains("финишировал") || lastLog.contains("победил") -> {
                            _state.value = _state.value.copy(
                                showNotification = true,
                                notificationTitle = "Победа",
                                notificationName = "Вы победили!",
                                notificationValue = ""
                            )
                        }
                    }
                }

                Log.d("GameViewModel", "Turn completed, currentPlayer: ${_state.value.currentPlayer?.id}")
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error making turn: ${e.message}", e)
            }
        }
    }


    fun dismissNotification() {
        _state.value = _state.value.copy(
            showNotification = false,
            notificationTitle = "",
            notificationName = "",
            notificationValue = ""
        )
    }

    fun resetGame() {
        viewModelScope.launch {
            try {
                engine.resetGame()
                _state.value = engine.getState()
                Log.d("GameViewModel", "Game reset")
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error resetting game: ${e.message}", e)
            }
        }
    }
}