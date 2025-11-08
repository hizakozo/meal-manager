package com.ken_stack.meal_manager_api.controller.gen.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 
 * @param mealId 食事ID
 * @param dishName 料理名
 * @param cookedAt 調理日時
 * @param memo メモ
 * @param imageId 画像ID
 * @param imageUrl 画像の配信URL
 * @param recipeId レシピID
 */
data class MealResponse(

    @get:JsonProperty("mealId", required = true) val mealId: java.util.UUID,

    @get:JsonProperty("dishName", required = true) val dishName: kotlin.String,

    @get:JsonProperty("cookedAt", required = true) val cookedAt: java.time.OffsetDateTime,

    @get:JsonProperty("memo", required = true) val memo: kotlin.String,

    @get:JsonProperty("imageId") val imageId: java.util.UUID? = null,

    @get:JsonProperty("imageUrl") val imageUrl: kotlin.String? = null,

    @get:JsonProperty("recipeId") val recipeId: java.util.UUID? = null
    ) {

}

