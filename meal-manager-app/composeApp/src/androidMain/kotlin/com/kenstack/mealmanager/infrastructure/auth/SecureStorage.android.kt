package com.kenstack.mealmanager.infrastructure.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Android実装: EncryptedSharedPreferencesを使用した安全なストレージ
 */
actual class SecureStorage(private val context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "meal_manager_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    actual suspend fun save(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    actual suspend fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    actual suspend fun delete(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    actual suspend fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
