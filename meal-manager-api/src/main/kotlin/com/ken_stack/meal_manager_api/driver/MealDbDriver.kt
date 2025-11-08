package com.ken_stack.meal_manager_api.driver

import com.ken_stack.meal_manager_api.driver.entities.MealEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MealDbDriver : ReactiveCrudRepository<MealEntity, UUID>
