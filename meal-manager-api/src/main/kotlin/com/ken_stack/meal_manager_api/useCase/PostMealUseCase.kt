package com.ken_stack.meal_manager_api.useCase

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.DomainError
import com.ken_stack.meal_manager_api.domain.model.Image
import com.ken_stack.meal_manager_api.domain.model.Meal
import com.ken_stack.meal_manager_api.domain.repository.MealRepository
import com.ken_stack.meal_manager_api.domain.service.ImageService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class PostMealUseCase(
    private val mealRepository: MealRepository,
    private val imageService: ImageService
) {
    suspend fun execute(input: PostMealInput): Either<PostMealUseCaseError, PostMealOutput> = either {
        // 画像がある場合、uploadバケットからdistributionバケットにコピーしてImage値オブジェクトを作成
        val image = input.imageId?.let { imageId ->
            try {
                imageService.copyToDistribution(imageId)
                Image.create().mapLeft { domainError ->
                    PostMealUseCaseErrors.ValidationError(domainError)
                }.bind()
            } catch (e: Exception) {
                raise(PostMealUseCaseErrors.FailedToCopyImage(e.message ?: "Unknown error"))
            }
        }

        // Mealドメインモデルを作成（バリデーション）
        val meal = Meal.create(
            dishName = input.dishName,
            cookedAt = input.cookedAt,
            memo = input.memo,
            image = image,
            recipeId = input.recipeId
        ).mapLeft { domainError ->
            PostMealUseCaseErrors.ValidationError(domainError)
        }.bind()

        // Repositoryに保存
        val savedMeal = try {
            mealRepository.save(meal)
        } catch (e: Exception) {
            raise(PostMealUseCaseErrors.RepositoryError(e.message ?: "Unknown error"))
        }

        // Outputに変換
        PostMealOutput(
            mealId = savedMeal.mealId.value,
            dishName = savedMeal.dishName.value,
            cookedAt = savedMeal.cookedAt.value,
            memo = savedMeal.memo.value,
            imageId = savedMeal.image?.imageId,
            imageUrl = savedMeal.image?.let { imageService.getUrl(it.imageId) },
            recipeId = savedMeal.recipeId?.value
        )
    }
}


sealed class PostMealUseCaseError(override val code: ErrorCode, override val message: String) : UseCaseError

sealed class PostMealUseCaseErrors {
    data class ValidationError(val domainError: DomainError) : PostMealUseCaseError(ErrorCode.VALIDATION_ERROR, domainError.message)
    data class FailedToCopyImage(override val message: String) : PostMealUseCaseError(ErrorCode.FAILED_TO_COPY_IMAGE, message)
    data class RepositoryError(override val message: String) : PostMealUseCaseError(ErrorCode.REPOSITORY_ERROR, message)
}

data class PostMealInput(
    val dishName: String,
    val cookedAt: LocalDateTime,
    val memo: String,
    val imageId: UUID?,
    val recipeId: UUID?
)

data class PostMealOutput(
    val mealId: UUID,
    val dishName: String,
    val cookedAt: LocalDateTime,
    val memo: String,
    val imageId: UUID?,
    val imageUrl: String?,
    val recipeId: UUID?
)
