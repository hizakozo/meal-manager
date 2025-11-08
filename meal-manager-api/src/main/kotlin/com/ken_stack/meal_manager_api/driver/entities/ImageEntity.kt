package com.ken_stack.meal_manager_api.driver.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("images")
data class ImageEntity(
    @Id
    @Column("image_id")
    val imageId: UUID,

    @Column("uploaded_at")
    val uploadedAt: LocalDateTime
)
