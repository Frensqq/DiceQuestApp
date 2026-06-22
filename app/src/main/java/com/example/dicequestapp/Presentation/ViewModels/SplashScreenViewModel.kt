package com.example.htm.Presentation.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Navigation.NavigationRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenViewModel(): ViewModel() {

    fun launch(navHostController: NavHostController, isOnline: Boolean){
        viewModelScope.launch {
            delay(2000)
            Log.d("Splash", UserRepository.Act.toString())

                if (UserRepository.Act) {
                    navHostController.navigate(NavigationRoutes.AUTH)
                } else {
                    navHostController.navigate(NavigationRoutes.AUTH)
                }
        }
    }

}