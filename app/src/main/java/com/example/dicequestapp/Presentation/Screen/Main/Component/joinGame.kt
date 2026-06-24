package com.example.dicequestapp.Presentation.Screen.Main.Component

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_ui.Button.ButtonBig
import com.example.dq_ui.Button.ButtonSmall
import com.example.dq_ui.Inputs.InputText
import com.example.dq_ui.Inputs.PlayerCountSelector
import com.example.dq_ui.UI.DiceQuestTheme
import com.example.dq_ui.UI.SpacerH
import com.example.dq_ui.UI.SpacerW
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun JoinGameDialog(
    navHostController: NavHostController,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val state = viewModel.state
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)


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
                Text(
                    text = "Подключение к игре",
                    style = DiceQuestTheme.typography.headlineLarge,
                    color = DiceQuestTheme.colors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                SpacerH(20)

                InputText(
                    text = state.gameId,
                    placeholder = "Введите код игры",
                    onValueChange = {
                        viewModel.updateState(state.copy(gameId = it)) },
                    isPass = false
                )

                SpacerH(16)

                Column() {
                    if (isScanning && cameraPermission.status.isGranted) {
                        SpacerH(8)
                        QRCodeScannerView(
                            onQrCodeScanned = { code ->
                                viewModel.updateState(
                                    state.copy(
                                        gameId = code,
                                        generalError = null
                                    )
                                )
                                isScanning = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                        SpacerH(8)
                    }

                    ButtonBig(
                        text = if (isScanning) "Скрыть сканер" else "Сканировать QR-код",
                        onClick = {
                            if (!cameraPermission.status.isGranted) {
                                cameraPermission.launchPermissionRequest()
                            } else {
                                isScanning = !isScanning
                            }
                        },
                        type = true
                    )

                }

                SpacerH(24)


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ButtonSmall(
                        onClick = {
                            viewModel.updateState(state.copy(statusGame = "Created"))
                            onDismiss()
                        },
                        text = "Войти",
                        type = false
                    )

                    SpacerW(5)

                    ButtonSmall(
                        onClick = {
                            viewModel.updateState(state.copy(username = ""))
                            onDismiss()
                        },
                        text = "Отмена",
                        type = true
                    )
                }


            }
        }
    }
}