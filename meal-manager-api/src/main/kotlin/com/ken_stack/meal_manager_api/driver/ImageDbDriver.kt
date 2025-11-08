package com.ken_stack.meal_manager_api.driver

import com.ken_stack.meal_manager_api.driver.entities.ImageEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface ImageDbDriver : ReactiveCrudRepository<ImageEntity, UUID> {

    @Query("""
        INSERT INTO images (image_id, uploaded_at)
        VALUES (:imageId, :uploadedAt)
        RETURNING *
    """)
    fun insert(imageId: UUID, uploadedAt: LocalDateTime): Mono<ImageEntity>
}
