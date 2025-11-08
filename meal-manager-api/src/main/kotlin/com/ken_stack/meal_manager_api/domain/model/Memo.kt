package com.ken_stack.meal_manager_api.domain.model

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.ken_stack.meal_manager_api.domain.DomainError

class Memo private constructor(
    val value: String
) {
    companion object {
        fun create(value: String): Either<DomainError, Memo> = either {
            ensure(value.length <= 1000) {
                DomainError("Memo cannot be longer than 1000 characters")
            }
            Memo(value)
        }

        fun of(value: String): Memo {
            return Memo(value)
        }
    }
}
