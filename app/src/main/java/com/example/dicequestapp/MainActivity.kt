package com.example.dicequestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dicequestapp.DI.networkModule
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.ui.theme.DiceQuestAppTheme
import com.example.dq_net_library.Network.NetworkMonitor
import com.example.dq_ui.UI.DiceQuestTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {

    val isOnline = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val networkModuleMon = NetworkMonitor(this)
        isOnline.value = networkModuleMon.isConnected()

        startKoin {
            androidContext(this@MainActivity)
            modules(networkModule)
        }

        UserRepository.init(this)

        enableEdgeToEdge()
        setContent {
            DiceQuestTheme {
                
            }
        }
    }
}

