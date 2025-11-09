package com.kenstack.mealmanager.feature.auth.model

/**
 * 認証状態を表すモデル
 */
sealed interface AuthState {
    /**
     * 初期状態（チェック中）
     */
    data object Initial : AuthState

    /**
     * 未認証
     */
    data object Unauthenticated : AuthState

    /**
     * 認証済み
     */
    data class Authenticated(
        val userId: String,
        val name: String?,
        val email: String?,
        val picture: String?
    ) : AuthState

    /**
     * ログイン処理中
     */
    data object LoggingIn : AuthState

    /**
     * ログアウト処理中
     */
    data object LoggingOut : AuthState

    /**
     * エラー
     */
    data class Error(val message: String) : AuthState
}
