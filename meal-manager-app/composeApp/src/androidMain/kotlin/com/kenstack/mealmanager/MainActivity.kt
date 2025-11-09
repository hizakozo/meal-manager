package com.kenstack.mealmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.kenstack.mealmanager.feature.auth.ui.LoginScreen
import com.kenstack.mealmanager.feature.auth.ui.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel = remember { LoginViewModel(this) }
            val authState by viewModel.authState.collectAsState()

            LoginScreen(
                authState = authState,
                onLoginClick = { viewModel.login() },
                onLogoutClick = { viewModel.logout() }
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}