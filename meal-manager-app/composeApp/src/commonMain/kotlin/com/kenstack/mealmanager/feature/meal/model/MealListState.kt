package com.kenstack.mealmanager.feature.meal.model

/**
 * Meal一覧画面の状態
 */
sealed class MealListState {
    /**
     * ローディング中
     */
    data object Loading : MealListState()

    /**
     * 取得成功
     */
    data class Success(val meals: List<Meal>) : MealListState()

    /**
     * エラー
     */
    data class Error(val message: String) : MealListState()
}
