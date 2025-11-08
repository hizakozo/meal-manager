package com.ken_stack.meal_manager_api.domain.repository

import com.ken_stack.meal_manager_api.domain.model.Auth0Sub
import com.ken_stack.meal_manager_api.domain.model.User
import com.ken_stack.meal_manager_api.domain.model.UserId

interface UserRepository {
    suspend fun save(user: User): User
    suspend fun findById(userId: UserId): User?
    suspend fun findByAuth0Sub(auth0Sub: Auth0Sub): User?
}
