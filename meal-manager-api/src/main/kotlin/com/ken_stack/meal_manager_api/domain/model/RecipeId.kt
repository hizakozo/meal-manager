package com.ken_stack.meal_manager_api.domain.model

import java.util.UUID

class RecipeId private constructor(
    val value: UUID
) {
    companion object {
        fun create(): RecipeId {
            return RecipeId(UUID.randomUUID())
        }

        fun of(value: UUID): RecipeId {
            return RecipeId(value)
        }
    }
}
