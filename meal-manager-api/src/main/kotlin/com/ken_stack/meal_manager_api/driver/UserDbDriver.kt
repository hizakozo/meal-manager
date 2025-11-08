package com.ken_stack.meal_manager_api.driver

import com.ken_stack.meal_manager_api.driver.entities.UserEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface UserDbDriver : ReactiveCrudRepository<UserEntity, UUID> {

    @Query("""
        INSERT INTO users (user_id, auth0_sub, created_at)
        VALUES (:userId, :auth0Sub, :createdAt)
        RETURNING *
    """)
    fun insert(userId: UUID, auth0Sub: String, createdAt: LocalDateTime): Mono<UserEntity>

    @Query("""
        SELECT * FROM users WHERE auth0_sub = :auth0Sub
    """)
    fun findByAuth0Sub(auth0Sub: String): Mono<UserEntity>
}
