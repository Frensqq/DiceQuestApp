package com.example.dicequestapp.Presentation.Screen.Auth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import com.example.dicequestapp.R
import com.example.dq_ui.Button.ButtonBig
import com.example.dq_ui.Inputs.InputText
import com.example.dq_ui.Inputs.InputsImage
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.UI.SpacerW
import com.example.htm.Presentation.viewModels.AuthViewModel

@Composable
fun RegisterScreen(navController: NavHostController, viewModel: AuthViewModel){

    val state = viewModel.state

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectImage(it, context)
        }
    }


    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9]{4,}@[A-Za-z0-9]{2,}\\.[A-Za-z]{2,3}$")
        val isValid = emailRegex.matches(email)
        viewModel.updateState(state.copy(
            emailError = if (!isValid && email.isNotEmpty()) "Неверный формат email" else null)
        )
        return isValid
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {



        Text("Регистрация!",
            style = DiceQuestTheme.typography.displayLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color =  DiceQuestTheme.colors.TextPrimary.copy(alpha = 0.8f)
        )

        SpacerH(30)

        InputText(
            text = state.email,
            onValueChange ={
                validateEmail(it)
                viewModel.updateState(state.copy(email = it))
            },
            isPass = false,
            isError = state.emailError != null,
            placeholder = "Email"
        )

        if (state.emailError != null) {
            Text(
                text = state.emailError!!,
                style = DiceQuestTheme.typography.labelLarge,
                color = DiceQuestTheme.colors.Error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        SpacerH(20)

        InputText(
            text = state.password,
            onValueChange = {
                viewModel.updateState(state.copy(password = it))
            },
            isPass = true,
            isError = false,
            placeholder = "Пароль"
        )

        if (state.generalError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(DiceQuestTheme.colors.Error.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(com.example.dq_ui.R.drawable.cross),
                        contentDescription = "error",
                        tint = DiceQuestTheme.colors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                    SpacerW(8)
                    Text(
                        text = state.generalError!!,
                        style = DiceQuestTheme.typography.labelLarge,
                        color = DiceQuestTheme.colors.Error
                    )
                }
            }
        }


        SpacerH(20)

        InputText(
            text = state.passwordConfirm,
            onValueChange = {
                viewModel.updateState(state.copy(passwordConfirm = it))
            },
            isPass = true,
            isError = false,
            placeholder = "Повторите пароль"
        )

        SpacerH(20)

        InputText(
            text = state.username,
            onValueChange ={
                validateEmail(it)
                viewModel.updateState(state.copy(username = it))
            },
            isPass = false,
            isError = false,
            placeholder = "UserName"
        )

        SpacerH(20)

        val imageUri = viewModel.selectedImageUri
        val painter = if (imageUri != null) {
            rememberAsyncImagePainter(model = imageUri)
        } else {
            null
        }

        InputsImage({
            galleryLauncher.launch("image/*")
        },
            painter,imageUri != null
            )

        if (imageUri != null) {

            SpacerH(5)

            Text(
                "Загрузить другой",
                style = DiceQuestTheme.typography.bodyLarge,
                color = DiceQuestTheme.colors.TextPrimary,
                modifier = Modifier.clickable {
                    galleryLauncher.launch("image/*")
                }
            )
        }

        SpacerH(20)

        ButtonBig(
            text = "Зарегистрироваться",
            onClick = {
                validateEmail(state.email)
                if (state.emailError == null){
                    viewModel.Registration(navController, context)
                }
            },
            enabled = state.email.isNotEmpty() && state.password.isNotEmpty(),
            type = true
        )

        SpacerH(20)

        Text("Уже есть аккаунт?, Войти",
            style = DiceQuestTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth().clickable(
                onClick = {
                    navController.navigate(NavigationRoutes.AUTH)
                }
            ),
            textAlign = TextAlign.Center,
            color =  DiceQuestTheme.colors.TextPrimary.copy(alpha = 0.7f)
        )
    }
}