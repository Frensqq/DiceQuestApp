package com.example.dicequestapp.DI


import com.example.dicequestapp.Domain.UseCase
import com.example.dicequestapp.Presentation.Screen.Game.Engine.GameEngine
import com.example.dicequestapp.Presentation.Screen.Game.Engine.Repository.IGameRepository
import com.example.dicequestapp.Presentation.Screen.Game.Engine.Repository.PocketBaseGameRepository
import com.example.dicequestapp.Presentation.ViewModels.GameViewModel
import com.example.dicequestapp.Presentation.ViewModels.MainViewModel
import com.example.dq_net_library.Data.Remoute.PBApi
import com.example.dq_net_library.Data.Remoute.PBApiServis
import com.example.dq_net_library.Data.Repository.PBRepositoryImpl
import com.example.dq_net_library.Domain.Repository.Repository
import com.example.dq_net_library.Network.NetworkConnected
import com.example.dq_net_library.Network.NetworkMonitor
import com.example.htm.Presentation.viewModels.AuthViewModel
import com.example.htm.Presentation.viewModels.SplashScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module
import kotlin.coroutines.EmptyCoroutineContext.get

val networkModule = module {
    single<PBApi> { PBApiServis.instance }
    single<NetworkConnected> { NetworkMonitor(androidContext()) }

    single<Repository> {
        PBRepositoryImpl(
            get<PBApi>(),
            get<NetworkConnected>(),
            androidContext()
        )
    }

    factory { UseCase(get()) }

    viewModel { SplashScreenViewModel() }
    viewModel { AuthViewModel(get()) }
    viewModel { MainViewModel(get()) }
}

val gameModule = module {
    // Только PocketBaseGameRepository
    single<IGameRepository> {
        PocketBaseGameRepository(get())
    }
    single { GameEngine(get(), isHost = true) }
    viewModel { GameViewModel(get()) }
}
