package com.example.netlibrary.Presentation.Screen.Auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import com.example.dicequestapp.R
import com.example.dq_ui.Button.ButtonBig
import com.example.dq_ui.Inputs.InputText
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.UI.SpacerW
import com.example.htm.Presentation.viewModels.AuthViewModel


@Composable
fun LogInScreen(navController: NavHostController, viewModel: AuthViewModel) {

    val state = viewModel.state

    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9]{4,}@[A-Za-z0-9]{2,}\\.[A-Za-z]{2,3}$")
        val isValid = emailRegex.matches(email)
        viewModel.updateState(state.copy(
            emailError = if (!isValid && email.isNotEmpty()) "Неверный формат email" else null)
        )
        return isValid
    }



    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {

        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null
        )
        SpacerH(40)

        Text("Вход!",
            style = DiceQuestTheme.typography.displayLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color =  DiceQuestTheme.colors.TextPrimary.copy(alpha = 0.8f)
        )

        SpacerH(15)

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


        SpacerH(50)

        ButtonBig(
            text = "Войти",
            onClick = {
                validateEmail(state.email)
                if (state.emailError == null){
                    viewModel.Auth(navController)
                }
            },
            enabled = state.email.isNotEmpty() && state.password.isNotEmpty(),
            type = true
        )

        SpacerH(20)

        Text("Зарегистрироваться",
            style = DiceQuestTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth().clickable(
                onClick = {
                    navController.navigate(NavigationRoutes.REGISTER)
                }
            ),
            textAlign = TextAlign.Center,
            color =  DiceQuestTheme.colors.TextPrimary.copy(alpha = 0.7f)
        )

        SpacerH(10)

        Text("Восстановить пароль",
            style = DiceQuestTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth().clickable(
                onClick = {
                    navController.navigate(NavigationRoutes.OTP_REQUEST)
                }
            ),
            textAlign = TextAlign.Center,
            color =  DiceQuestTheme.colors.TextPrimary.copy(alpha = 0.7f)
        )




    }
}