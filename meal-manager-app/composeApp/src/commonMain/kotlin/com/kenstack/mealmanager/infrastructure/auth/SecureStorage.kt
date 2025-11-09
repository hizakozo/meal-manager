package com.kenstack.mealmanager.infrastructure.auth

/**
 * プラットフォーム固有のセキュアストレージ
 * Android: EncryptedSharedPreferences
 * iOS: Keychain (将来実装)
 */
expect class SecureStorage {
    suspend fun save(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun delete(key: String)
    suspend fun clear()
}
