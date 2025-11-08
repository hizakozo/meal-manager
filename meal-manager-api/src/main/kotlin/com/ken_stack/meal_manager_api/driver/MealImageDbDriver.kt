package com.ken_stack.meal_manager_api.driver

import com.ken_stack.meal_manager_api.driver.entities.MealImageEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MealImageDbDriver : ReactiveCrudRepository<MealImageEntity, UUID>
