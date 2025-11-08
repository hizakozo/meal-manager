package com.ken_stack.meal_manager_api.driver

import com.ken_stack.meal_manager_api.driver.entities.MealEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface MealDbDriver : ReactiveCrudRepository<MealEntity, UUID> {

    @Query("""
        INSERT INTO meals (meal_id, user_id, dish_name, cooked_at, memo)
        VALUES (:mealId, :userId, :dishName, :cookedAt, :memo)
        RETURNING *
    """)
    fun insert(mealId: UUID, userId: UUID, dishName: String, cookedAt: LocalDateTime, memo: String): Mono<MealEntity>

    @Query("""
        SELECT * FROM meals
        WHERE user_id = :userId
        ORDER BY cooked_at DESC
    """)
    fun findAllByUserId(userId: UUID): Flux<MealEntity>
}
