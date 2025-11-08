package com.ken_stack.meal_manager_api.domain.repository

import com.ken_stack.meal_manager_api.domain.model.Meal
import com.ken_stack.meal_manager_api.domain.model.UserId
import java.time.LocalDate
import java.util.UUID

interface MealRepository {
    suspend fun save(meal: Meal): Meal
    suspend fun findById(mealId: UUID): Meal?
    suspend fun findAll(userId: UserId, startDate: LocalDate?, endDate: LocalDate?): List<Meal>
}
