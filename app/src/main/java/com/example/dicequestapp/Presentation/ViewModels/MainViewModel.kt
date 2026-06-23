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
import com.example.dq_net_library.Domain.Model.NetworkResult
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.io.copyTo

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
}