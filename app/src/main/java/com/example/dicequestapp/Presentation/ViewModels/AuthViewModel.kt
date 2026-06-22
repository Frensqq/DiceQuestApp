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


}