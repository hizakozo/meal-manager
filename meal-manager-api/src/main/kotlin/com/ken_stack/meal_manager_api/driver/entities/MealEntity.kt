package com.ken_stack.meal_manager_api.driver.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("meals")
data class MealEntity(
    @Id
    @Column("meal_id")
    val mealId: UUID,

    @Column("dish_name")
    val dishName: String,

    @Column("cooked_at")
    val cookedAt: LocalDateTime,

    @Column("memo")
    val memo: String
)
