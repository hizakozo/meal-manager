package com.ken_stack.meal_manager_api.domain.model

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.DomainError

@JvmInline
value class Auth0Sub private constructor(val value: String) {
    companion object {
        fun create(value: String): Either<DomainError, Auth0Sub> = either {
            if (value.isBlank()) {
                raise(DomainError("Auth0 sub cannot be blank"))
            }
            Auth0Sub(value)
        }

        fun of(value: String): Auth0Sub = Auth0Sub(value)
    }
}
