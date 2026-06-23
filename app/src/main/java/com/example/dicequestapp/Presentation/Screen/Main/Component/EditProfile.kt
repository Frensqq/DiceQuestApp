package com.example.dicequestapp.Presentation.Screen.Main.Component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_ui.Button.ButtonSmall
import com.example.dq_ui.Inputs.InputText
import com.example.dq_ui.Inputs.InputsImage
import com.example.dq_ui.R
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.UI.SpacerW

@Composable
fun EditProfileDialog(
    navHostController: NavHostController,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val state = viewModel.state
    val userData = state.User

    val context = LocalContext.current


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectImage(it, context)
        }
    }

    // Получаем URL текущего аватара, если он есть
    val currentAvatarUrl = state.User?.let { user ->
        if (user.avatar.isNotEmpty()) {
            viewModel.getImageUrl("users", user.id, user.avatar)
        } else null
    }

    // Определяем, какое изображение показывать: новое выбранное или текущее
    val displayImageUri = viewModel.selectedImageUri
    val painter = when {
        displayImageUri != null -> rememberAsyncImagePainter(model = displayImageUri)
        currentAvatarUrl != null -> rememberAsyncImagePainter(model = currentAvatarUrl)
        else -> null
    }
    val hasImage = displayImageUri != null || currentAvatarUrl != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DiceQuestTheme.colors.Surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    text = "Редактирование профиля",
                    style = DiceQuestTheme.typography.headlineLarge,
                    color = DiceQuestTheme.colors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                SpacerH(20)



                // Поле для изображения
                InputsImage(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    painter = painter,
                    state = hasImage
                )
                Text(
                    if (hasImage) "Загрузить другой" else "Выбрать фото",
                    style = DiceQuestTheme.typography.bodyLarge,
                    color = DiceQuestTheme.colors.Primary,
                    modifier = Modifier.clickable {
                        galleryLauncher.launch("image/*")
                    }
                )

                SpacerH(16)


                InputText(
                    text = state.username,
                    placeholder = "Введите имя пользователя",
                    onValueChange = { viewModel.updateState(state.copy(username = it))},
                    isPass = false
                )

                SpacerH(24)


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ButtonSmall(
                        onClick = {
                            viewModel.UpdateProfile(navHostController, context)
                            onDismiss()
                        },
                        text = "Сохранить",
                        type = false
                    )

                    SpacerW(5)

                    ButtonSmall(
                        onClick = {
                            viewModel.updateState(state.copy(username = ""))
                            onDismiss()
                                  },
                        text = "Выйти",
                        type = true
                    )
                }
            }
        }
    }
}