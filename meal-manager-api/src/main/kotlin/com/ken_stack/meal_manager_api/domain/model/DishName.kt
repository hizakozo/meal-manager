package com.ken_stack.meal_manager_api.domain.model

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.ken_stack.meal_manager_api.domain.DomainError

class DishName private constructor(
    val value: String
) {
    companion object {
        fun create(value: String): Either<DomainError, DishName> = either {
            ensure(value.isNotBlank()) {
                DomainError("Dish name cannot be blank")
            }
            ensure(value.length <= 100) {
                DomainError("Dish name cannot be longer than 100 characters")
            }
            DishName(value)
        }

        fun of(value: String): DishName {
            return DishName(value)
        }
    }
}
