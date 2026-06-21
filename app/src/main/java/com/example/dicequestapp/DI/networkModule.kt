package com.example.dicequestapp.DI


import com.example.dicequestapp.Domain.UseCase
import com.example.dq_net_library.Data.Remoute.PBApi
import com.example.dq_net_library.Data.Remoute.PBApiServis
import com.example.dq_net_library.Data.Repository.PBRepositoryImpl
import com.example.dq_net_library.Domain.Repository.Repository
import com.example.dq_net_library.Network.NetworkConnected
import com.example.dq_net_library.Network.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val networkModule = module{
    single<PBApi>{ PBApiServis.instance }
    single<NetworkConnected> { NetworkMonitor(androidContext()) }

    single<Repository> {
        PBRepositoryImpl(
            get<PBApi>(),
            get<NetworkConnected>(),
            androidContext()
        )
    }

    factory { UseCase(get()) }

}