package com.example.dicequestapp.Presentation.Screen.Auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.dq_ui.Inputs.OtpInput
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.htm.Presentation.viewModels.AuthViewModel

@Composable
fun OtpResponse(navController: NavHostController, viewModel: AuthViewModel) {

    val state = viewModel.state


    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null
        )
        SpacerH(40)

        Text("Введите сообщение из email",
            style = DiceQuestTheme.typography.displayLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color =  DiceQuestTheme.colors.TextPrimary.copy(alpha = 0.8f)
        )

        SpacerH(15)

        OtpInput(4,
            {
                viewModel.updateState(state.copy(otpCode = it))
            }
        )

        SpacerH(50)

        ButtonBig(
            text = "Отправить запрос",
            onClick = {
                viewModel.ResponseOTP(navController)
            },
            enabled = state.email.isNotEmpty(),
            type = true
        )

        SpacerH(20)

        Text("Войти",
            style = DiceQuestTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth().clickable(
                onClick = {
                    navController.navigate(NavigationRoutes.AUTH)
                }
            ),
            textAlign = TextAlign.Center,
            color =  DiceQuestTheme.colors.TextPrimary.copy(alpha = 0.7f)
        )

        SpacerH(10)

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
    }
}