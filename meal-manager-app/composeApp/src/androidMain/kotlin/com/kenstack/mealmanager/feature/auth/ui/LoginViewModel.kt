package com.kenstack.mealmanager.feature.auth.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenstack.mealmanager.feature.auth.model.AuthState
import com.kenstack.mealmanager.infrastructure.auth.AuthManager
import com.kenstack.mealmanager.infrastructure.auth.SecureStorage
import com.kenstack.mealmanager.infrastructure.auth.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ログイン画面のViewModel（Android専用）
 */
class LoginViewModel(
    context: Context
) : ViewModel() {

    private val secureStorage = SecureStorage(context)
    private val tokenManager = TokenManager(secureStorage)
    private val authManager = AuthManager(context, tokenManager)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * 認証状態をチェック
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            val hasToken = tokenManager.hasToken()
            if (hasToken && !tokenManager.isTokenExpired()) {
                // トークンが有効な場合、ユーザー情報を取得
                val accessToken = tokenManager.getAccessToken()
                if (accessToken != null) {
                    val userInfo = authManager.getUserInfo(accessToken)
                    _authState.value = AuthState.Authenticated(
                        userId = userInfo["sub"] as? String ?: "",
                        name = userInfo["name"] as? String,
                        email = userInfo["email"] as? String,
                        picture = userInfo["picture"] as? String
                    )
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * ログイン
     */
    fun login() {
        viewModelScope.launch {
            _authState.value = AuthState.LoggingIn
            val credentials = authManager.login()
            authManager.handleLoginSuccess(credentials)

            // ユーザー情報を取得
            val userInfo = authManager.getUserInfo(credentials.accessToken)
            _authState.value = AuthState.Authenticated(
                userId = userInfo["sub"] as? String ?: "",
                name = userInfo["name"] as? String,
                email = userInfo["email"] as? String,
                picture = userInfo["picture"] as? String
            )
        }
    }

    /**
     * ログアウト
     */
    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.LoggingOut
            authManager.logout()
            authManager.handleLogoutSuccess()
            _authState.value = AuthState.Unauthenticated
        }
    }
}
