package com.ken_stack.meal_manager_api.useCase

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.repository.MealRepository
import com.ken_stack.meal_manager_api.domain.service.ImageStore
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class GetMealsUseCase(
    private val mealRepository: MealRepository,
    private val imageStore: ImageStore
) {
    suspend fun execute(input: GetMealsInput): Either<GetMealsUseCaseError, GetMealsOutput> = either {
        // Repositoryから取得
        val meals = try {
            mealRepository.findAll(input.startDate, input.endDate)
        } catch (e: Exception) {
            raise(GetMealsUseCaseErrors.RepositoryError(e.message ?: "Unknown error"))
        }

        // DTOに変換
        val mealDtos = meals.map { meal ->
            MealDto(
                mealId = meal.mealId.value,
                dishName = meal.dishName.value,
                cookedAt = meal.cookedAt.value,
                memo = meal.memo.value,
                imageId = meal.image?.imageId,
                imageUrl = meal.image?.let { imageStore.getDistributionUrl(it.imageId) },
                recipeId = meal.recipeId?.value
            )
        }

        GetMealsOutput(meals = mealDtos)
    }
}

interface GetMealsUseCaseError

sealed class GetMealsUseCaseErrors : GetMealsUseCaseError {
    data class RepositoryError(val message: String) : GetMealsUseCaseErrors()
}

data class GetMealsInput(
    val startDate: LocalDate?,
    val endDate: LocalDate?
)

data class GetMealsOutput(
    val meals: List<MealDto>
)

data class MealDto(
    val mealId: UUID,
    val dishName: String,
    val cookedAt: LocalDateTime,
    val memo: String,
    val imageId: UUID?,
    val imageUrl: String?,
    val recipeId: UUID?
)
