package com.ken_stack.meal_manager_api.domain.model

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.DomainError
import java.time.LocalDateTime

class CookedAt private constructor(
    val value: LocalDateTime
) {
    companion object {
        fun create(value: LocalDateTime): Either<DomainError, CookedAt> = either {
            CookedAt(value)
        }

        fun of(value: LocalDateTime): CookedAt {
            return CookedAt(value)
        }
    }
}
