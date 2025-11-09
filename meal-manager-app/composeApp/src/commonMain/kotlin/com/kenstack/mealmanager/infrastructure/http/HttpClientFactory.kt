package com.kenstack.mealmanager.infrastructure.http

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * HttpClientのファクトリー
 */
object HttpClientFactory {

    /**
     * HttpClientを作成
     *
     * @param baseUrl APIのベースURL
     * @param getAccessToken アクセストークンを取得する関数
     */
    fun create(
        baseUrl: String,
        getAccessToken: suspend () -> String?
    ): HttpClient {
        val authInterceptor = AuthInterceptor(getAccessToken)

        return HttpClient {
            // ベースURL設定
            defaultRequest {
                url(baseUrl)
            }

            // JSON設定
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // ログ設定（開発時のみ）
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }

            // 認証インターセプター
            install(authInterceptor.plugin)

            // デフォルトリクエスト設定
            install(DefaultRequest) {
                headers.append("Content-Type", "application/json")
            }

            // タイムアウト設定
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }
        }
    }
}
