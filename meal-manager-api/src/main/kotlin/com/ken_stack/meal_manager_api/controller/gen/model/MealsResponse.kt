package com.ken_stack.meal_manager_api.controller.gen.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty
import com.ken_stack.meal_manager_api.controller.gen.model.MealResponse

/**
 * 
 * @param meals 
 */
data class MealsResponse(

    @get:JsonProperty("meals", required = true) val meals: kotlin.collections.List<MealResponse>
    ) {

}

