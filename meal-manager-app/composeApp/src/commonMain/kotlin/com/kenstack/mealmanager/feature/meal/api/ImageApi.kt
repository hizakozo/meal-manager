package com.kenstack.mealmanager.feature.meal.api

import com.kenstack.mealmanager.feature.meal.model.UploadUrlResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * 画像アップロード用の署名付きURLを取得
 */
suspend fun getUploadUrl(client: HttpClient): UploadUrlResponse {
    return client.get("/meal-manager-api/images/upload-url").body()
}

/**
 * S3に画像をアップロード
 */
suspend fun uploadImageToS3(client: HttpClient, presignedUrl: String, imageData: ByteArray) {
    client.put(presignedUrl) {
        contentType(ContentType.Image.JPEG)
        setBody(imageData)
    }
}
