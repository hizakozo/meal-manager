package com.kenstack.mealmanager.feature.meal.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 食事ドメインモデル
 */
@Serializable
data class Meal(
    val mealId: String,
    val dishName: String,
    val cookedAt: Instant,
    val memo: String,
    val imageId: String? = null,
    val imageUrl: String? = null,
    val recipeId: String? = null
)

/**
 * 食事一覧レスポンス
 */
@Serializable
data class MealListResponse(
    val meals: List<Meal>
)

/**
 * 食事作成リクエスト
 */
@Serializable
data class CreateMealRequest(
    val dishName: String,
    val cookedAt: Instant,
    val memo: String,
    val imageId: String? = null,
    val recipeId: String? = null
)
