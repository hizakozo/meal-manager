package com.ken_stack.meal_manager_api.driver

import com.ken_stack.meal_manager_api.driver.entities.MealImageEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface MealImageDbDriver : ReactiveCrudRepository<MealImageEntity, UUID> {

    @Query("""
        INSERT INTO meal_images (meal_id, image_id)
        VALUES (:mealId, :imageId)
        RETURNING *
    """)
    fun insert(mealId: UUID, imageId: UUID): Mono<MealImageEntity>
}
