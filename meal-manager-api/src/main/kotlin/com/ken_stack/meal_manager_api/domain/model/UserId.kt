package com.ken_stack.meal_manager_api.domain.model

import java.util.UUID

@JvmInline
value class UserId(val value: UUID) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID())
        fun of(value: UUID): UserId = UserId(value)
    }
}
