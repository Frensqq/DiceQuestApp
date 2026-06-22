package com.example.dicequestapp.Presentation.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dicequestapp.Domain.UserRepository
import com.example.dicequestapp.Presentation.Screen.Auth.OtpRequest
import com.example.dicequestapp.Presentation.Screen.Auth.OtpResponse
import com.example.dicequestapp.Presentation.Screen.Auth.RegisterScreen
import com.example.dicequestapp.Presentation.Screen.System.NoInternetScreen
import com.example.dicequestapp.Presentation.Screen.System.SplashScreen
import com.example.htm.Presentation.viewModels.AuthViewModel
import com.example.htm.Presentation.viewModels.SplashScreenViewModel
import com.example.netlibrary.Presentation.Screen.Auth.LogInScreen
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun Navigation(isOnline: Boolean){

    val NavController = rememberNavController()
    val ViewModelSplash: SplashScreenViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()

    LaunchedEffect(isOnline) {
        delay(2000)
        if (isOnline) {
            if (UserRepository.Act){
                NavController.navigate(NavigationRoutes.AUTH)
            }else{
                NavController.navigate(NavigationRoutes.AUTH)
            }
        } else {
            NavController.navigate(NavigationRoutes.NO_INTERNET_SCREEN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = NavController, startDestination =  NavigationRoutes.SPLASH_SCREEN ){

        composable(NavigationRoutes.SPLASH_SCREEN) {
            SplashScreen(ViewModelSplash,NavController, isOnline)
        }

        composable(NavigationRoutes.NO_INTERNET_SCREEN) {
            NoInternetScreen()
        }


        composable(NavigationRoutes.AUTH) {
            LogInScreen(NavController, authViewModel)
        }

        composable(NavigationRoutes.REGISTER) {
            RegisterScreen(NavController, authViewModel)
        }

        composable(NavigationRoutes.MAIN) {

        }

        composable(NavigationRoutes.PROFILE) {

        }


        composable(NavigationRoutes.OTP_AUTH) {
            OtpResponse(NavController, authViewModel)
        }

        composable(NavigationRoutes.OTP_REQUEST) {
            OtpRequest(NavController, authViewModel)
        }

        composable(NavigationRoutes.CHANGE_PASS) {

        }

        composable(NavigationRoutes.CHANGE_PASS_CONFIRM) {

        }

        composable(NavigationRoutes.CREATE_USER) {

        }

    }

}