package com.kenstack.mealmanager.infrastructure.auth

import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.kenstack.mealmanager.BuildConfig
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Auth0を使った認証管理クラス（Android専用）
 */
class AuthManager(
    private val context: Context,
    private val tokenManager: TokenManager
) {
    private val account = Auth0(
        BuildConfig.AUTH0_CLIENT_ID,
        BuildConfig.AUTH0_DOMAIN
    )

    private val authenticationClient = AuthenticationAPIClient(account)

    /**
     * ログイン（ブラウザを使用したOAuth認証）
     */
    suspend fun login(): Credentials = suspendCoroutine { continuation ->
        WebAuthProvider.login(account)
            .withScheme(BuildConfig.AUTH0_SCHEME)
            .withScope("openid profile email offline_access")
            .start(context, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    continuation.resume(result)
                }

                override fun onFailure(error: AuthenticationException) {
                    continuation.resumeWithException(error)
                }
            })
    }

    /**
     * ログアウト
     */
    suspend fun logout() = suspendCoroutine { continuation ->
        WebAuthProvider.logout(account)
            .withScheme(BuildConfig.AUTH0_SCHEME)
            .start(context, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) {
                    continuation.resume(Unit)
                }

                override fun onFailure(error: AuthenticationException) {
                    continuation.resumeWithException(error)
                }
            })
    }

    /**
     * トークンをリフレッシュ
     */
    suspend fun refreshToken(refreshToken: String): Credentials = suspendCoroutine { continuation ->
        authenticationClient.renewAuth(refreshToken)
            .start(object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    continuation.resume(result)
                }

                override fun onFailure(error: AuthenticationException) {
                    continuation.resumeWithException(error)
                }
            })
    }

    /**
     * ユーザー情報を取得
     */
    suspend fun getUserInfo(accessToken: String): Map<String, Any> = suspendCoroutine { continuation ->
        authenticationClient.userInfo(accessToken)
            .start(object : Callback<com.auth0.android.result.UserProfile, AuthenticationException> {
                override fun onSuccess(result: com.auth0.android.result.UserProfile) {
                    val userInfo = mapOf(
                        "sub" to (result.getId() ?: ""),
                        "name" to (result.name ?: ""),
                        "email" to (result.email ?: ""),
                        "picture" to (result.pictureURL ?: "")
                    )
                    continuation.resume(userInfo)
                }

                override fun onFailure(error: AuthenticationException) {
                    continuation.resumeWithException(error)
                }
            })
    }

    /**
     * ログイン後の処理（トークンを保存）
     */
    suspend fun handleLoginSuccess(credentials: Credentials) {
        val expiresInSeconds = credentials.expiresAt.time / 1000 - (System.currentTimeMillis() / 1000)
        tokenManager.saveTokens(
            accessToken = credentials.accessToken,
            refreshToken = credentials.refreshToken,
            expiresIn = expiresInSeconds
        )
    }

    /**
     * ログアウト後の処理（トークンをクリア）
     */
    suspend fun handleLogoutSuccess() {
        tokenManager.clearTokens()
    }
}
