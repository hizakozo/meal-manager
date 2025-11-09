package com.kenstack.mealmanager.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kenstack.mealmanager.feature.auth.model.AuthState

/**
 * ログイン画面
 */
@Composable
fun LoginScreen(
    authState: AuthState,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLoginSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // ログイン成功時にナビゲーション
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (authState) {
                is AuthState.Initial -> {
                    CircularProgressIndicator()
                }

                is AuthState.Unauthenticated -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Meal Manager",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            text = "献立管理アプリへようこそ",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ログイン")
                        }
                    }
                }

                is AuthState.LoggingIn -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("ログイン中...")
                    }
                }

                is AuthState.Authenticated -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "ログイン成功",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        authState.name?.let {
                            Text("ようこそ、${it}さん")
                        }
                        authState.email?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onLogoutClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ログアウト")
                        }
                    }
                }

                is AuthState.LoggingOut -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("ログアウト中...")
                    }
                }

                is AuthState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "エラー",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = authState.message,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onLoginClick) {
                            Text("再試行")
                        }
                    }
                }
            }
        }
    }
}
