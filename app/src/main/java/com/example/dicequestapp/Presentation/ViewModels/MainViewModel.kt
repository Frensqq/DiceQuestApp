package com.example.dicequestapp.Presentation.ViewModels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.dicequestapp.Domain.UseCase
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import com.example.dicequestapp.Presentation.State.MainState
import com.example.dq_net_library.Data.Remoute.PBApiServis
import com.example.dq_net_library.Domain.Model.Cell.Cell
import com.example.dq_net_library.Domain.Model.Cell.CreateCell
import com.example.dq_net_library.Domain.Model.Game.AddPlayer
import com.example.dq_net_library.Domain.Model.Game.RedactGame
import com.example.dq_net_library.Domain.Model.Game.RequestCreateGame
import com.example.dq_net_library.Domain.Model.NetworkResult
import com.example.dq_net_library.Domain.Model.Player.CreatePlayer
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.io.copyTo
import kotlin.random.Random

class MainViewModel(private val UseCase: UseCase): ViewModel() {
    private val _state = mutableStateOf(MainState())
    val state: MainState get() = _state.value

    fun updateState(newState: MainState) {
        _state.value = newState
    }

    fun getImageUrl(collection: String, id: String, nameImg: String): String {
        return UseCase.getImageUrl(collection, id, nameImg)
    }

    fun GetUser(){
        viewModelScope.launch {
            PBApiServis.setToken(UserRepository.Token)
            updateState(state.copy(isLoading = true, error = null))
            try {
                Log.e("GetUser Debug", "userId = ${UserRepository.UserId}")
                when(val response = UseCase.getUser(
                    UserRepository.UserId
                )){
                    is NetworkResult.Success -> {
                        updateState(state.copy(isSuccess = true))
                        state.User = response.data
                        UserRepository.userName = response.data.userName
                        Log.d("GetUser Debug" , response.data.id)
                    }
                    is NetworkResult.Error -> {
                        updateState(state.copy(isLoading = false, error = response.errorResponse.message))
                        Log.e("GetUser Error", response.errorResponse.message)

                    }
                    is NetworkResult.NoInternet -> {
                        updateState(state.copy(isNotInternet = true))
                        Log.e("GetUser NoInternet", state.error.toString())
                    }

                }
                Log.d("GetUser", state.error.toString())
            }
            catch (e: Exception){
                Log.e("GetUser ViewModel", e.message.toString())
            }
        }
    }

    fun UpdateProfile(
        navController: NavHostController,
        context: Context
    ) {
        viewModelScope.launch {
            updateState(state.copy(isLoading = true, error = null))

            try {
                var avatarFile: File? = null
                selectedImageUri?.let { uri ->
                    avatarFile = uriToFile(context, uri)
                    if (avatarFile == null) {
                        Log.e("CreateUser", "Failed to convert URI to file")
                    }
                }
                Log.d("UpdateProfile", "Avatar file: ${avatarFile?.path}")
                Log.d("UpdateProfile", "Avatar file exists: ${avatarFile?.exists()}")

                val response = UseCase.updateProfile(
                    userId = UserRepository.UserId,
                    token = UserRepository.Token,
                    userName = state.username,
                    avatarFile = avatarFile,
                )
                when (response) {
                    is NetworkResult.Success -> {
                        Log.d("UpdateProfile", "Success: ${response.data}")
                        Log.d("UpdateProfile", "Avatar field: ${response.data.avatar}")
                        UserRepository.UserId = response.data.id
                        UserRepository.userName = response.data.userName
                        updateState(state.copy(User = response.data))
                        avatarFile?.delete()
                        navController.navigate(NavigationRoutes.PROFILE)
                    }
                    is NetworkResult.Error -> {
                        updateState(
                            state.copy(
                                isLoading = false,
                                error = response.errorResponse.message
                            )
                        )
                        Log.e("UpdateProfile Error", "Message: ${response.errorResponse.message}")
                        avatarFile?.delete()
                    }

                    is NetworkResult.NoInternet -> {
                        updateState(state.copy(isNotInternet = true))
                        Log.e("UpdateProfile", "No internet connection")
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateProfile", "Exception: ${e.message}", e)
                updateState(state.copy(isLoading = false, error = e.message))
            }
        }
    }



    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedImageFile by mutableStateOf<File?>(null)
    var selectedImageName by mutableStateOf("")
    fun selectImage(uri: Uri, context: Context) {
        selectedImageUri = uri
        selectedImageName = getFileNameFromUri(context, uri)
        // Конвертируем Uri в File
        selectedImageFile = uriToFile(context, uri)
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex) ?: "image_${System.currentTimeMillis()}.jpg"
        } ?: "image_${System.currentTimeMillis()}.jpg"
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("AuthViewModel", "Failed to open input stream for URI: $uri")
                return null
            }

            // Создаем уникальное имя файла
            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            val tempFile = File(context.cacheDir, fileName)

            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            inputStream.close()

            Log.d("AuthViewModel", "File created: ${tempFile.absolutePath}")
            Log.d("AuthViewModel", "File size: ${tempFile.length()} bytes")

            tempFile
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error converting URI to file: ${e.message}", e)
            null
        }
    }


    fun createSinglePlayerGame(
        gameName: String,
        botCount: Int = 3,
        onGameCreated: (gameId: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d("CreateGame", "=== НАЧАЛО СОЗДАНИЯ ИГРЫ ===")

                // 1. Создаём игроков
                val playerIds = mutableListOf<String>()
                var creatorPlayerId = ""

                // Игрок (человек) - создатель
                val playerResult = UseCase.createPlayer(
                    CreatePlayer(
                        userId = UserRepository.UserId,
                        gameId = "",
                        isBot = false,
                        position = 0,
                        shield = false,
                        finished = false,
                        turnOrder = 0
                    )
                )
                if (playerResult is NetworkResult.Success) {
                    val player = playerResult.data
                    playerIds.add(player.id)
                    creatorPlayerId = player.id
                    UserRepository.PlayerId = player.id
                    Log.d("CreateGame", "Игрок-создатель создан: ${player.id}")
                } else {
                    Log.e("CreateGame", "Ошибка создания игрока")
                    return@launch
                }

                // Боты
                for (i in 1..botCount) {
                    val botResult = UseCase.createPlayer(
                        CreatePlayer(
                            userId = "",
                            gameId = "",
                            isBot = true,
                            position = 0,
                            shield = false,
                            finished = false,
                            turnOrder = i
                        )
                    )
                    if (botResult is NetworkResult.Success) {
                        playerIds.add(botResult.data.id)
                        Log.d("CreateGame", "Бот $i создан: ${botResult.data.id}")
                    }
                }

                Log.d("CreateGame", "Всего игроков: ${playerIds.size}")
                Log.d("CreateGame", "Creator Player ID: $creatorPlayerId")

                // 2. Создаём игру с существующими игроками
                val gameRequest = RequestCreateGame(
                    name = gameName,
                    players = playerIds,
                    countPlayer = playerIds.size.toDouble(),
                    multiplayer = false,
                    type = "single",
                    status = "waiting",
                    creator = creatorPlayerId,
                    start = "",
                    end = "",
                    countCell = 70.0,
                    currentPlayer = playerIds.firstOrNull() ?: "",
                    currentTurn = 0.0,
                    diceValue = 0.0
                )

                Log.d("CreateGame", "Запрос на создание игры: $gameRequest")

                when (val gameResult = UseCase.createGame(gameRequest)) {
                    is NetworkResult.Success -> {
                        val game = gameResult.data
                        Log.d("CreateGame", "Игра создана: ${game.id}")

                        UserRepository.GameId = game.id

                        // 3. Генерируем поле
                        val cells = generateBoardForGame(game.id)
                        if (cells.isEmpty()) {
                            Log.e("CreateGame", "Не удалось сгенерировать поле")
                            return@launch
                        }
                        Log.d("CreateGame", "Поле сгенерировано: ${cells.size} ячеек")

                        // 4. Обновляем статус игры
                        val patchRequest = RedactGame(
                            players = playerIds,
                            type = "single",
                            status = "playing",
                            start = "",
                            end = "",
                            countCell = 70.0,
                            currentPlayer = playerIds.firstOrNull() ?: "",
                            currentTurn = 0.0,
                            diceValue = 0.0
                        )

                        UseCase.patchGames(game.id, patchRequest)

                        Log.d("CreateGame", "=== ИГРА СОЗДАНА УСПЕШНО ===")
                        onGameCreated(game.id)
                    }
                    is NetworkResult.Error -> {
                        Log.e("CreateGame", "Ошибка создания игры: ${gameResult.errorResponse.message}")
                    }
                    is NetworkResult.NoInternet -> {
                        Log.e("CreateGame", "Нет интернета")
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateGame", "Исключение: ${e.message}", e)
            }
        }
    }
    /**
     * Генерация поля для игры
     */
    private suspend fun generateBoardForGame(gameId: String): List<Cell> {
        val countCells = 70
        val cellsToCreate = createBoardCells(gameId, countCells)
        val createdCells = mutableListOf<Cell>()

        for (cellRequest in cellsToCreate) {
            when (val response = UseCase.createCell(cellRequest)) {
                is NetworkResult.Success -> {
                    createdCells.add(response.data)
                }
                is NetworkResult.Error -> {
                    Log.e("GenerateBoard", "Error: ${response.errorResponse.message}")
                    return emptyList()
                }
                is NetworkResult.NoInternet -> {
                    Log.e("GenerateBoard", "No internet")
                    return emptyList()
                }
            }
        }

        return createdCells
    }

    /**
     * Создание ячеек для поля
     */
    private fun createBoardCells(gameId: String, count: Int): List<CreateCell> {
        val result = mutableListOf<CreateCell>()
        val width = 7
        var number = 1
        var row = 0
        var leftToRight = true

        while (number <= count) {
            val cols = if (leftToRight) {
                0 until width
            } else {
                (width - 1 downTo 0)
            }

            for (col in cols) {
                if (number > count) break

                val (type, value) = getCellTypeAndValue(number, count)

                result += CreateCell(
                    gameId = gameId,
                    number = number,
                    type = type,
                    value = value,
                    row = row,
                    col = col
                )

                number++
            }

            if (number > count) break

            row++

            val verticalCol = if (leftToRight) width - 1 else 0

            val (type, value) = getCellTypeAndValue(number, count)

            result += CreateCell(
                gameId = gameId,
                number = number,
                type = type,
                value = value,
                row = row,
                col = verticalCol
            )

            number++
            row++
            leftToRight = !leftToRight
        }

        return result
    }

    /**
     * Определение типа и значения ячейки
     */
    private fun getCellTypeAndValue(number: Int, totalCount: Int): Pair<String, String> {
        if (number == 1) return "start" to ""
        if (number == totalCount) return "finish" to ""

        return when (Random.nextInt(100)) {
            in 0..19 -> "bonus" to listOf("+3", "+5", "+7").random()
            in 20..39 -> "penalty" to listOf("-3", "-5", "-7").random()
            in 40..49 -> "event" to listOf("+3", "+5", "-3", "-5").random()
            in 50..54 -> "protection" to "shield"
            else -> "empty" to ""
        }
    }




}