package com.kenstack.mealmanager.feature.meal.model

import kotlinx.serialization.Serializable

/**
 * 画像アップロード用URL取得レスポンス
 */
@Serializable
data class UploadUrlResponse(
    val imageId: String,
    val presignedUrl: String
)
