package com.kenstack.mealmanager.feature.meal.api

import com.kenstack.mealmanager.feature.meal.model.CreateMealRequest
import com.kenstack.mealmanager.feature.meal.model.Meal
import com.kenstack.mealmanager.feature.meal.model.MealListResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

/**
 * 食事一覧を取得
 */
suspend fun getMeals(client: HttpClient): List<Meal> {
    val response = client.get("/meal-manager-api/meals").body<MealListResponse>()
    return response.meals
}

/**
 * 食事詳細を取得
 */
suspend fun getMeal(client: HttpClient, mealId: String): Meal {
    return client.get("/meal-manager-api/meals/$mealId").body()
}

/**
 * 食事を作成
 */
suspend fun createMeal(client: HttpClient, request: CreateMealRequest): Meal {
    return client.post("/meal-manager-api/meals") {
        setBody(request)
    }.body()
}
