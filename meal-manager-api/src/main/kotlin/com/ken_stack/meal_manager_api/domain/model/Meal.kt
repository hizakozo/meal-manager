package com.ken_stack.meal_manager_api.domain.model

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.DomainError
import java.time.LocalDateTime
import java.util.UUID

class Meal private constructor(
    val mealId: MealId,
    val dishName: DishName,
    val cookedAt: CookedAt,
    val memo: Memo,
    val image: Image?,
    val recipeId: RecipeId?
) {
    companion object {
        fun create(
            dishName: String,
            cookedAt: LocalDateTime,
            memo: String,
            image: Image?,
            recipeId: UUID?
        ): Either<DomainError, Meal> = either {
            val validatedDishName = DishName.create(dishName).bind()
            val validatedCookedAt = CookedAt.create(cookedAt).bind()
            val validatedMemo = Memo.create(memo).bind()

            Meal(
                mealId = MealId.create(),
                dishName = validatedDishName,
                cookedAt = validatedCookedAt,
                memo = validatedMemo,
                image = image,
                recipeId = recipeId?.let { RecipeId.of(it) }
            )
        }

        fun of(
            mealId: UUID,
            dishName: String,
            cookedAt: LocalDateTime,
            memo: String,
            image: Image?,
            recipeId: UUID?
        ): Meal {
            return Meal(
                mealId = MealId.of(mealId),
                dishName = DishName.of(dishName),
                cookedAt = CookedAt.of(cookedAt),
                memo = Memo.of(memo),
                image = image,
                recipeId = recipeId?.let { RecipeId.of(it) }
            )
        }
    }

    fun update(
        dishName: String,
        cookedAt: LocalDateTime,
        memo: String,
        image: Image?,
        recipeId: UUID?
    ): Either<DomainError, Meal> = either {
        val validatedDishName = DishName.create(dishName).bind()
        val validatedCookedAt = CookedAt.create(cookedAt).bind()
        val validatedMemo = Memo.create(memo).bind()

        Meal(
            mealId = this@Meal.mealId,
            dishName = validatedDishName,
            cookedAt = validatedCookedAt,
            memo = validatedMemo,
            image = image,
            recipeId = recipeId?.let { RecipeId.of(it) }
        )
    }
}
