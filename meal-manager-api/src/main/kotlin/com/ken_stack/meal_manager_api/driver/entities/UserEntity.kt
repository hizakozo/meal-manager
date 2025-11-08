package com.ken_stack.meal_manager_api.driver.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("users")
data class UserEntity(
    @Id
    val userId: UUID,
    val auth0Sub: String,
    val createdAt: LocalDateTime
)
