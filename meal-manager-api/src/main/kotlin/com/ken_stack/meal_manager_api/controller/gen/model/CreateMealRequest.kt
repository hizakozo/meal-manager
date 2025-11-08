package com.ken_stack.meal_manager_api.controller.gen.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 
 * @param dishName 料理名
 * @param cookedAt 調理日時
 * @param memo メモ
 * @param imageId 画像ID（事前にアップロード完了済みのもの）
 * @param recipeId レシピID
 */
data class CreateMealRequest(

    @get:JsonProperty("dishName", required = true) val dishName: kotlin.String,

    @get:JsonProperty("cookedAt", required = true) val cookedAt: java.time.OffsetDateTime,

    @get:JsonProperty("memo", required = true) val memo: kotlin.String,

    @get:JsonProperty("imageId") val imageId: java.util.UUID? = null,

    @get:JsonProperty("recipeId") val recipeId: java.util.UUID? = null
    ) {

}

