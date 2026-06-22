package com.example.dicequestapp.Presentation.Screen.System

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dq_ui.R
import com.example.dq_ui.UI.DiceQuestGradients
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH


@Composable
fun NoInternetScreen(){

    val infiniteTransition = rememberInfiniteTransition(label = "blink")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_alpha"
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .background(DiceQuestGradients.PurpleGlow),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
        ) {

        Icon(painter = painterResource(R.drawable.no_internet),
            contentDescription = null,
            tint = DiceQuestTheme.colors.Error,
            modifier = Modifier.size(50.dp).alpha(alpha)

        )

        SpacerH(20)

        Text("No Internet\nT_T", style = DiceQuestTheme
            .typography
            .titleLarge
            .copy(letterSpacing = 2.sp, fontSize = 45.sp),
            color= DiceQuestTheme.colors.TextPrimary,
            textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
fun TestScreenNoInternetScreen(){
    NoInternetScreen()
}
