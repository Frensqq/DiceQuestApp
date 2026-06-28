package com.example.dicequestapp.Presentation.ViewModels

import android.util.Log
import androidx.compose.runtime.mutableStateOf
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

    /**
     * Загрузка игры
     */
    fun loadGame() {
        val gameId = UserRepository.GameId
        Log.d("GameViewModel", "loadGame: gameId=$gameId")

        if (gameId.isEmpty()) {
            Log.e("GameViewModel", "GameId is empty")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("GameViewModel", "Initializing engine with gameId: $gameId")
                engine.initGame(gameId)
                _state.value = engine.getState()
                Log.d("GameViewModel", "Game loaded, cells: ${_state.value.cells.size}")
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error loading game: ${e.message}", e)
                _state.value = state.copy(error = e.message)
            }
        }
    }

    /**
     * Обновление состояния
     */
    fun updateState(newState: GameState) {
        _state.value = newState
    }

    /**
     * Сделать ход
     */
    fun makeTurn(playerId: String, diceValue: Int) {
        viewModelScope.launch {
            try {
                Log.d("GameViewModel", "makeTurn: playerId=$playerId, diceValue=$diceValue")
                engine.makeTurn(playerId, diceValue)
                _state.value = engine.getState()
                Log.d("GameViewModel", "Turn completed, currentPlayer: ${_state.value.currentPlayer?.id}")
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error making turn: ${e.message}", e)
                _state.value = state.copy(error = e.message)
            }
        }
    }

    /**
     * Сброс игры
     */
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