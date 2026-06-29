package com.example.dicequestapp.Presentation.ViewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import com.example.dicequestapp.Presentation.Screen.Game.Component.GameNotification
import com.example.dicequestapp.Presentation.Screen.Game.Engine.GameEngine
import com.example.dicequestapp.Presentation.State.GameState
import kotlinx.coroutines.launch

class GameViewModel(
    private val engineDi: GameEngine
) : ViewModel() {

    var notification by mutableStateOf<GameNotification?>(null)
        private set

    fun showNotification(
        title: String,
        text: String,
        value: String = ""
    ) {
        notification = GameNotification(
            title,
            text,
            value
        )
    }

    fun hideNotification() {
        notification = null
    }

    private val _state = mutableStateOf(GameState())
    val state: GameState get() = _state.value

    var engine: GameEngine = engineDi

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
                _state.value = _state.value.copy(
                    isTurnProcessing = true
                )

                val currentPlayer = _state.value.currentPlayer
                val isBot = currentPlayer?.isBot ?: false

                engine.makeTurn(playerId, diceValue)

                _state.value = engine.getState().copy(
                    isTurnProcessing = false
                )

                engine.consumeNotification()?.let {
                    _state.value = _state.value.copy(
                        showNotification = true,
                        notificationTitle = it.title,
                        notificationName = it.text,
                        notificationValue = it.value
                    )
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isTurnProcessing = false
                )
                Log.e("GameViewModel", e.message ?: "")
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

    fun finishGameAndExit(navController: NavHostController) {
        viewModelScope.launch {
            try {
                Log.d("GameViewModel", "=== finishGameAndExit START ===")
                Log.d("GameViewModel", "isCreator: ${state.isCreator}")
                Log.d("GameViewModel", "PlayerId from Repository: ${UserRepository.PlayerId}")

                if (state.isCreator) {
                    Log.d("GameViewModel", "Создатель, удаляем всё")
                    engine.deleteGameData()
                    Log.d("GameViewModel", "Клетки и игроки удалены")
                } else {
                    val playerId = UserRepository.PlayerId
                    if (playerId.isNotEmpty()) {
                        Log.d("GameViewModel", "Не создатель, удаляем только игрока: $playerId")
                        engine.deletePlayer(playerId)
                        Log.d("GameViewModel", "Игрок удалён")
                    } else {
                        Log.d("GameViewModel", "PlayerId пустой, пропускаем удаление")
                    }
                }

                UserRepository.GameId = ""
                UserRepository.PlayerId = ""

                Log.d("GameViewModel", "Переход на главный экран")
                navController.navigate(NavigationRoutes.MAIN) {
                    popUpTo(0) { inclusive = true }
                }

            } catch (e: Exception) {
                Log.e("GameViewModel", "Ошибка при завершении игры: ${e.message}", e)
            }
        }
    }

    fun syncGameState() {
        viewModelScope.launch {
            try {
                val gameId = UserRepository.GameId
                if (gameId.isEmpty()) return@launch

                // Перезагружаем игру через engine
                engine.initGame(gameId)
                _state.value = engine.getState()

                Log.d("GameViewModel", "Sync: isMyTurn = ${_state.value.isMyTurn}")
            } catch (e: Exception) {
                Log.e("GameViewModel", "Sync error: ${e.message}", e)
            }
        }
    }


}