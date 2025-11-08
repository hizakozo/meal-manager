package com.ken_stack.meal_manager_api.useCase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.ken_stack.meal_manager_api.domain.model.UserId
import com.ken_stack.meal_manager_api.domain.repository.MealRepository
import com.ken_stack.meal_manager_api.domain.service.ImageStore
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class GetMealUseCase(
    private val mealRepository: MealRepository,
    private val imageStore: ImageStore
) {
    suspend fun execute(input: GetMealInput): Either<GetMealUseCaseError, GetMealOutput> = either {
        // 食事を取得
        val meal = ensureNotNull(mealRepository.findById(input.mealId)) {
            GetMealUseCaseErrors.MealNotFound()
        }

        // 認可チェック: 食事が要求ユーザーのものか確認
        ensure(meal.userId == input.userId) {
            GetMealUseCaseErrors.Forbidden()
        }

        // OutputDTOに変換
        val imageUrl = meal.image?.let { imageStore.getDistributionUrl(it.imageId) }

        GetMealOutput(
            mealDto = MealDto(
                mealId = meal.mealId.value,
                dishName = meal.dishName.value,
                cookedAt = meal.cookedAt.value,
                memo = meal.memo.value,
                imageId = meal.image?.imageId,
                imageUrl = imageUrl,
                recipeId = meal.recipeId?.value
            )
        )
    }
}

sealed class GetMealUseCaseError(override val code: ErrorCode, override val message: String) : UseCaseError

sealed class GetMealUseCaseErrors {
    data class MealNotFound(val mealId: UUID? = null) : GetMealUseCaseError(
        ErrorCode.MEAL_NOT_FOUND,
        "Meal not found"
    )

    data class Forbidden(val mealId: UUID? = null) : GetMealUseCaseError(
        ErrorCode.FORBIDDEN,
        "You don't have permission to access this meal"
    )
}

data class GetMealInput(
    val userId: UserId,
    val mealId: UUID
)

data class GetMealOutput(
    val mealDto: MealDto
)
