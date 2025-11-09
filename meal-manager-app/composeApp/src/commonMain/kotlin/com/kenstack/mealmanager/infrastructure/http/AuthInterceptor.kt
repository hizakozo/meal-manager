package com.kenstack.mealmanager.infrastructure.http

import io.ktor.client.*
import io.ktor.client.plugins.api.*

/**
 * 認証インターセプター
 * リクエストに自動的にAuthorizationヘッダーを追加する
 */
class AuthInterceptor(
    private val getAccessToken: suspend () -> String?
) {

    /**
     * HttpClientにインストールするプラグイン
     */
    val plugin = createClientPlugin("AuthInterceptor") {
        onRequest { request, _ ->
            val token = getAccessToken()
            if (token != null) {
                request.headers.append("Authorization", "Bearer $token")
            }
        }
    }
}
