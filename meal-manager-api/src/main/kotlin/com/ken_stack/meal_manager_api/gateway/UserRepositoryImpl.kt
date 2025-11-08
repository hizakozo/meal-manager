package com.ken_stack.meal_manager_api.gateway

import com.ken_stack.meal_manager_api.domain.model.Auth0Sub
import com.ken_stack.meal_manager_api.domain.model.User
import com.ken_stack.meal_manager_api.domain.model.UserId
import com.ken_stack.meal_manager_api.domain.repository.UserRepository
import com.ken_stack.meal_manager_api.driver.UserDbDriver
import com.ken_stack.meal_manager_api.driver.entities.UserEntity
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val userDbDriver: UserDbDriver
) : UserRepository {

    override suspend fun save(user: User): User {
        val savedEntity = userDbDriver.insert(
            userId = user.userId.value,
            auth0Sub = user.auth0Sub.value,
            createdAt = user.createdAt
        ).awaitFirstOrNull() ?: throw IllegalStateException("Failed to save user")

        return savedEntity.toDomain()
    }

    override suspend fun findById(userId: UserId): User? {
        return userDbDriver.findById(userId.value)
            .awaitFirstOrNull()
            ?.toDomain()
    }

    override suspend fun findByAuth0Sub(auth0Sub: Auth0Sub): User? {
        return userDbDriver.findByAuth0Sub(auth0Sub.value)
            .awaitFirstOrNull()
            ?.toDomain()
    }

    private fun UserEntity.toDomain(): User {
        return User.of(
            userId = UserId.of(this.userId),
            auth0Sub = Auth0Sub.of(this.auth0Sub),
            createdAt = this.createdAt
        )
    }
}
