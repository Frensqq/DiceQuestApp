package com.example.dicequestapp.Presentation.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dicequestapp.Presentation.Screen.System.NoInternetScreen

@Composable
fun Navigation(isOnline: Boolean){

    val NavController = rememberNavController()

    LaunchedEffect(isOnline) {
        if (isOnline) {
            NavController.popBackStack() 
        } else {
            NavController.navigate(NavigationRoutes.NO_INTERNET_SCREEN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = NavController, startDestination = if (isOnline) NavigationRoutes.MAIN else NavigationRoutes.NO_INTERNET_SCREEN){

        composable(NavigationRoutes.SPLASH_SCREEN) {

        }

        composable(NavigationRoutes.AUTH) {

        }

        composable(NavigationRoutes.REGISTER) {

        }

        composable(NavigationRoutes.MAIN) {

        }

        composable(NavigationRoutes.PROFILE) {

        }

        composable(NavigationRoutes.NO_INTERNET_SCREEN) {
            NoInternetScreen()
        }

        composable(NavigationRoutes.OTP_AUTH) {

        }

        composable(NavigationRoutes.OTP_REQUEST) {

        }

        composable(NavigationRoutes.CHANGE_PASS) {

        }

        composable(NavigationRoutes.CHANGE_PASS_CONFIRM) {

        }

        composable(NavigationRoutes.CREATE_USER) {

        }

    }

}