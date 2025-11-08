package com.ken_stack.meal_manager_api.domain.model

import java.util.UUID

class MealId private constructor(
    val value: UUID
) {
    companion object {
        fun create(): MealId {
            return MealId(UUID.randomUUID())
        }

        fun of(value: UUID): MealId {
            return MealId(value)
        }
    }
}
