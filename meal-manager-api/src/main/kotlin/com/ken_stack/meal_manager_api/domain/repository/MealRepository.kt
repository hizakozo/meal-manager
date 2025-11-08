package com.ken_stack.meal_manager_api.domain.repository

import com.ken_stack.meal_manager_api.domain.model.Meal
import java.time.LocalDate
import java.util.UUID

interface MealRepository {
    suspend fun save(meal: Meal): Meal
    suspend fun findById(mealId: UUID): Meal?
    suspend fun findAll(startDate: LocalDate?, endDate: LocalDate?): List<Meal>
}
