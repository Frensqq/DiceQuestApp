package com.example.htm.Presentation.viewModels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.dicequestapp.Domain.UseCase
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import com.example.dq_net_library.Data.Remoute.PBApiServis
import com.example.dq_net_library.Domain.Model.NetworkResult
import com.example.netlibrary.Presentation.State.AuthState
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AuthViewModel(private val UseCase: UseCase): ViewModel() {

    private val _state = mutableStateOf(AuthState())
    val state: AuthState get() = _state.value

    fun updateState(newState: AuthState) {
        _state.value = newState
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



    fun validatePasswords(): Boolean {
        val password = _state.value.password
        val confirm = _state.value.passwordConfirm

        val passwordError = if (password.isNotEmpty() && password.length < 6) "Пароль минимум 6 символов" else null
        val confirmError = if (confirm.isNotEmpty() && password != confirm) "Пароли не совпадают" else null

        _state.value = _state.value.copy(
            passwordError = passwordError,
            confirmPasswordError = confirmError
        )
        return passwordError == null && confirmError == null
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

    fun Auth(navController: NavHostController) {
        viewModelScope.launch {
            updateState(state.copy(isLoading = true, generalError = null))
            try {
                when(val response = UseCase.loginIn(
                    email = state.email,
                    password = state.password,
                )){
                    is NetworkResult.Success -> {
                        UserRepository.UserId = response.data.record.id
                        UserRepository.Act = true
                        UserRepository.userName = response.data.record.userName
                        UserRepository.Token = response.data.token
                        PBApiServis.setToken(UserRepository.Token)

                        navController.navigate(NavigationRoutes.MAIN)

                        updateState(state.copy(
                            isLoading = false,
                            password = "",
                            passwordConfirm = "",
                            email = "",
                            username = ""
                        ))
                    }
                    is NetworkResult.Error -> {
                        updateState(state.copy(
                            isLoading = false,
                            generalError = "Ошибка авторизации. Введите данные повторно."
                        ))
                        Log.e("Auth Error", response.errorResponse.message)
                    }
                    is NetworkResult.NoInternet -> {
                        updateState(state.copy(
                            isNotInternet = true,
                            generalError = "Нет подключения к интернету"
                        ))
                    }
                }
            } catch (e: Exception) {
                updateState(state.copy(
                    isLoading = false,
                    generalError = "Ошибка авторизации. Введите данные повторно."
                ))
            }
        }
    }

    fun Registration(navController: NavHostController,context: Context ) {
        viewModelScope.launch {
            updateState(state.copy(isLoading = true, generalError = null))
            try {

                var avatarFile: File? = null
                selectedImageUri?.let { uri ->
                    avatarFile = uriToFile(context, uri)
                    if (avatarFile == null) {
                        Log.e("CreateUser", "Failed to convert URI to file")
                    }
                }

                when(val response = UseCase.registration(
                    email = state.email,
                    password = state.password,
                    passwordConfirm = state.passwordConfirm,
                    username = state.username,
                    avatar = avatarFile
                )){
                    is NetworkResult.Success -> {
                        UserRepository.UserId = response.data.id
                        UserRepository.Email = state.email
                        AuthInRegister()
                        navController.navigate(NavigationRoutes.MAIN)
                    }
                    is NetworkResult.Error -> {
                        updateState(state.copy(
                            isLoading = false,
                            generalError = "Ошибка регистрации. Попробуйте снова."
                        ))
                        Log.e("Reg Error", response.errorResponse.message)
                    }
                    is NetworkResult.NoInternet -> {
                        updateState(state.copy(
                            isNotInternet = true,
                            generalError = "Нет подключения к интернету"
                        ))
                    }
                }
            } catch (e: Exception) {
                updateState(state.copy(
                    isLoading = false,
                    generalError = "Ошибка регистрации. Введите данные повторно."
                ))
            }
        }
    }

    fun AuthInRegister()   {
        viewModelScope.launch {
            try {
                when(val response = UseCase.loginIn(
                    email = state.email,
                    password = state.password,
                )){
                    is NetworkResult.Success -> {
                        UserRepository.UserId = response.data.record.id
                        UserRepository.Act = true
                        UserRepository.Token = response.data.token
                        PBApiServis.setToken(UserRepository.Token)
                    }
                    is NetworkResult.Error -> {
                        updateState(state.copy(isLoading = false, error = response.errorResponse.message))
                        Log.e("AuthInRegister Error", response.errorResponse.message)

                    }
                    is NetworkResult.NoInternet -> {
                        updateState(state.copy(isNotInternet = true))
                        Log.e("AuthInRegister NoInternet", state.error.toString())
                    }

                }
                Log.d("AuthInRegister", state.error.toString())
            }
            catch (e: Exception){
                Log.e("AuthInRegister ViewModel", e.message.toString())
            }
        }
    }
}