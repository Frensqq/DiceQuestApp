package com.example.dicequestapp.Presentation.Screen.System

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.dicequestapp.R
import com.example.dq_ui.UI.DiceQuestColors

import com.example.dq_ui.UI.DiceQuestGradients
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.htm.Presentation.viewModels.SplashScreenViewModel

@Composable
fun SplashScreen(viewModel: SplashScreenViewModel, navHostController: NavHostController, isOnline: Boolean){

    viewModel.launch(navHostController, isOnline)

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(
            colors = listOf(
                DiceQuestTheme.colors.PrimaryDark,
                DiceQuestTheme.colors.SurfaceVariant
            )
        )),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 60.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth)

            Text("Захватывающие приключения", style = DiceQuestTheme.typography.titleLarge,
                color= DiceQuestTheme.colors.TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

