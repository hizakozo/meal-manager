package com.kenstack.mealmanager.feature.meal.model

/**
 * Meal作成画面の状態
 */
sealed class MealCreateState {
    /**
     * アイドル状態
     */
    data object Idle : MealCreateState()

    /**
     * 保存中
     */
    data object Saving : MealCreateState()

    /**
     * 保存成功
     */
    data class Success(val mealId: String) : MealCreateState()

    /**
     * エラー
     */
    data class Error(val message: String) : MealCreateState()
}

/**
 * 画像アップロードの状態
 */
sealed class ImageUploadState {
    /**
     * アイドル状態
     */
    data object Idle : ImageUploadState()

    /**
     * アップロード中
     */
    data object Uploading : ImageUploadState()

    /**
     * アップロード成功
     */
    data class Success(val imageId: String, val localUri: String) : ImageUploadState()

    /**
     * エラー
     */
    data class Error(val message: String) : ImageUploadState()
}
