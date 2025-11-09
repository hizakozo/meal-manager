package com.kenstack.mealmanager.infrastructure.auth

/**
 * JWTトークンの管理クラス（Android専用）
 * - トークンの保存・取得・削除
 * - トークンの有効期限チェック
 */
class TokenManager(
    private val secureStorage: SecureStorage
) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }

    /**
     * トークンを保存
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long? = null
    ) {
        secureStorage.save(KEY_ACCESS_TOKEN, accessToken)
        refreshToken?.let {
            secureStorage.save(KEY_REFRESH_TOKEN, it)
        }
        expiresIn?.let { expiresInSeconds ->
            // 現在時刻のミリ秒 + 有効期限秒数
            val currentTimeMillis = System.currentTimeMillis()
            val expiresAtMillis = currentTimeMillis + (expiresInSeconds * 1000)
            secureStorage.save(KEY_EXPIRES_AT, expiresAtMillis.toString())
        }
    }

    /**
     * アクセストークンを取得
     */
    suspend fun getAccessToken(): String? {
        return secureStorage.get(KEY_ACCESS_TOKEN)
    }

    /**
     * リフレッシュトークンを取得
     */
    suspend fun getRefreshToken(): String? {
        return secureStorage.get(KEY_REFRESH_TOKEN)
    }

    /**
     * トークンが有効期限切れかチェック
     */
    suspend fun isTokenExpired(): Boolean {
        val expiresAtString = secureStorage.get(KEY_EXPIRES_AT) ?: return true
        val expiresAtMillis = try {
            expiresAtString.toLong()
        } catch (e: Exception) {
            return true
        }
        val currentTimeMillis = System.currentTimeMillis()
        return currentTimeMillis >= expiresAtMillis
    }

    /**
     * トークンが存在するかチェック
     */
    suspend fun hasToken(): Boolean {
        return getAccessToken() != null
    }

    /**
     * 全てのトークンをクリア（ログアウト時）
     */
    suspend fun clearTokens() {
        secureStorage.delete(KEY_ACCESS_TOKEN)
        secureStorage.delete(KEY_REFRESH_TOKEN)
        secureStorage.delete(KEY_EXPIRES_AT)
    }
}
