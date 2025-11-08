package com.ken_stack.meal_manager_api.driver.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("meal_images")
data class MealImageEntity(
    @Id
    @Column("meal_id")
    val mealId: UUID,

    @Column("image_id")
    val imageId: UUID
)
