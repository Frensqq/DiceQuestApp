package com.example.dicequestapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.dicequestapp.DI.gameModule
import com.example.dicequestapp.DI.networkModule
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.Navigation
import com.example.dq_net_library.Network.NetworkMonitor
import com.example.dq_ui.UI.DiceQuestTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {

    private val isOnline = MutableStateFlow(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val networkMonitor = NetworkMonitor(this)
        isOnline.value = networkMonitor.isConnected()

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                lifecycleScope.launch { isOnline.value = true }
            }
            override fun onLost(network: Network) {
                lifecycleScope.launch { isOnline.value = false }
            }
        })

        startKoin {
            androidContext(this@MainActivity)
            modules(networkModule)
            modules(gameModule)
        }

        UserRepository.init(this)

        enableEdgeToEdge()
        setContent {
            DiceQuestTheme {
                val onlineStatus by isOnline.collectAsState(initial = false)
                Navigation(onlineStatus)
            }
        }
    }
}

