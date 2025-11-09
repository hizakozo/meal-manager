package com.kenstack.mealmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kenstack.mealmanager.feature.auth.ui.LoginScreen
import com.kenstack.mealmanager.feature.auth.ui.LoginViewModel
import com.kenstack.mealmanager.feature.meal.ui.MealListScreen
import com.kenstack.mealmanager.feature.meal.viewmodel.MealListViewModel
import com.kenstack.mealmanager.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val loginViewModel = remember { LoginViewModel(this) }
            val authState by loginViewModel.authState.collectAsState()

            NavHost(
                navController = navController,
                startDestination = Screen.Login.route
            ) {
                // ログイン画面
                composable(Screen.Login.route) {
                    LoginScreen(
                        authState = authState,
                        onLoginClick = { loginViewModel.login() },
                        onLogoutClick = { loginViewModel.logout() },
                        onLoginSuccess = {
                            navController.navigate(Screen.MealList.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Meal一覧画面
                composable(Screen.MealList.route) {
                    val mealListViewModel = remember { MealListViewModel(this@MainActivity) }
                    val mealListState by mealListViewModel.state.collectAsState()

                    MealListScreen(
                        state = mealListState,
                        onMealClick = { mealId ->
                            navController.navigate(Screen.MealDetail.createRoute(mealId))
                        },
                        onCreateClick = {
                            navController.navigate(Screen.MealCreate.route)
                        },
                        onRefresh = {
                            mealListViewModel.refresh()
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}