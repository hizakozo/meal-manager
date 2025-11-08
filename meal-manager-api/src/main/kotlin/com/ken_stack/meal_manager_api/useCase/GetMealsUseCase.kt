package com.ken_stack.meal_manager_api.useCase

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.model.UserId
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
        // Repositoryから取得（SQL時点でuserIdでフィルタリング）
        val meals = mealRepository.findAll(input.userId, input.startDate, input.endDate)

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

sealed class GetMealsUseCaseError(override val code: ErrorCode, override val message: String) : UseCaseError

data class GetMealsInput(
    val userId: UserId,
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
