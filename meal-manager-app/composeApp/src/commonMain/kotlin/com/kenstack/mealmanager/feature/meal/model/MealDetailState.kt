package com.kenstack.mealmanager.feature.meal.model

/**
 * Meal詳細画面の状態
 */
sealed class MealDetailState {
    /**
     * ローディング中
     */
    data object Loading : MealDetailState()

    /**
     * 取得成功
     */
    data class Success(val meal: Meal) : MealDetailState()

    /**
     * エラー
     */
    data class Error(val message: String) : MealDetailState()
}
