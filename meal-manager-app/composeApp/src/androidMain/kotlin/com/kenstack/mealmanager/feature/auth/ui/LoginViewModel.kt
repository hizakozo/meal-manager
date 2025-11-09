package com.kenstack.mealmanager.feature.auth.ui

import android.content.Context
import android.util.Log
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

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val secureStorage = SecureStorage(context)
    private val tokenManager = TokenManager(secureStorage)
    private val authManager = AuthManager(context, tokenManager)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        Log.d(TAG, "LoginViewModel init started")
        checkAuthStatus()
        Log.d(TAG, "LoginViewModel init completed")
    }

    /**
     * 認証状態をチェック
     */
    private fun checkAuthStatus() {
        Log.d(TAG, "checkAuthStatus started")
        viewModelScope.launch {
            try {
                Log.d(TAG, "Checking token...")
                val hasToken = tokenManager.hasToken()
                Log.d(TAG, "hasToken: $hasToken")

                if (hasToken && !tokenManager.isTokenExpired()) {
                    Log.d(TAG, "Token is valid, extracting user ID from JWT...")
                    val userId = tokenManager.getUserIdFromToken()
                    if (userId != null) {
                        Log.d(TAG, "User ID extracted from JWT: $userId")
                        _authState.value = AuthState.Authenticated(
                            userId = userId,
                            name = null,
                            email = null,
                            picture = null
                        )
                        Log.d(TAG, "AuthState set to Authenticated")
                    } else {
                        Log.d(TAG, "Failed to extract user ID from token, setting Unauthenticated")
                        _authState.value = AuthState.Unauthenticated
                    }
                } else {
                    Log.d(TAG, "Token is invalid or expired, setting Unauthenticated")
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in checkAuthStatus: ${e.message}", e)
                Log.e(TAG, "Error type: ${e.javaClass.name}")
                e.printStackTrace()
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
            try {
                val credentials = authManager.login()
                authManager.handleLoginSuccess(credentials)

                // JWTトークンからユーザーIDを取得（ネットワーク呼び出しを回避）
                val userId = tokenManager.getUserIdFromToken()
                if (userId != null) {
                    _authState.value = AuthState.Authenticated(
                        userId = userId,
                        name = null,
                        email = null,
                        picture = null
                    )
                } else {
                    _authState.value = AuthState.Error("Failed to extract user ID from token")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in login: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
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
