package com.ken_stack.meal_manager_api.domain.model

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.DomainError
import java.time.LocalDateTime

class User private constructor(
    val userId: UserId,
    val auth0Sub: Auth0Sub,
    val createdAt: LocalDateTime
) {
    companion object {
        fun create(auth0Sub: String): Either<DomainError, User> = either {
            val auth0SubValue = Auth0Sub.create(auth0Sub).bind()
            User(
                userId = UserId.generate(),
                auth0Sub = auth0SubValue,
                createdAt = LocalDateTime.now()
            )
        }

        fun of(
            userId: UserId,
            auth0Sub: Auth0Sub,
            createdAt: LocalDateTime
        ): User {
            return User(userId, auth0Sub, createdAt)
        }
    }
}
