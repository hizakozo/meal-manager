package com.ken_stack.meal_manager_api.controller

import com.ken_stack.meal_manager_api.controller.ErrorController.toResponse
import com.ken_stack.meal_manager_api.controller.gen.api.IMealManagerApiController
import com.ken_stack.meal_manager_api.controller.gen.model.CreateMealRequest
import com.ken_stack.meal_manager_api.controller.gen.model.MealResponse
import com.ken_stack.meal_manager_api.controller.gen.model.MealsResponse
import com.ken_stack.meal_manager_api.controller.gen.model.UploadCompleteResponse
import com.ken_stack.meal_manager_api.controller.gen.model.UploadUrlResponse
import com.ken_stack.meal_manager_api.domain.repository.UserRepository
import com.ken_stack.meal_manager_api.domain.service.ImageStore
import com.ken_stack.meal_manager_api.useCase.GetMealInput
import com.ken_stack.meal_manager_api.useCase.GetMealOutput
import com.ken_stack.meal_manager_api.useCase.GetMealUseCase
import com.ken_stack.meal_manager_api.useCase.GetMealsInput
import com.ken_stack.meal_manager_api.useCase.GetMealsOutput
import com.ken_stack.meal_manager_api.useCase.GetMealsUseCase
import com.ken_stack.meal_manager_api.useCase.GetUploadUrlOutput
import com.ken_stack.meal_manager_api.useCase.GetUploadUrlUseCase
import com.ken_stack.meal_manager_api.useCase.MealDto
import com.ken_stack.meal_manager_api.useCase.PostMealInput
import com.ken_stack.meal_manager_api.useCase.PostMealOutput
import com.ken_stack.meal_manager_api.useCase.PostMealUseCase
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.time.ZoneId
import java.util.UUID

@Component
class MealManagerApiControllerImpl(
    private val getUploadUrlUseCase: GetUploadUrlUseCase,
    private val postMealUseCase: PostMealUseCase,
    private val getMealsUseCase: GetMealsUseCase,
    private val getMealUseCase: GetMealUseCase,
    private val imageStore: ImageStore,
    userRepository: UserRepository
) : WithAuthController(userRepository), IMealManagerApiController {

    override suspend fun getUploadUrl(request: ServerRequest): ServerResponse {
        return getUploadUrlUseCase.execute().fold(
            { error -> error.toResponse() },
             { output -> ServerResponse.ok().bodyValueAndAwait(output.toResponse()) }
        )
    }

    override suspend fun completeUpload(request: ServerRequest): ServerResponse {
        val imageId = UUID.fromString(request.pathVariable("imageId"))

        return try {
            imageStore.copyToDistribution(imageId)
            val distributionUrl = imageStore.getDistributionUrl(imageId)
            ServerResponse.ok().bodyValueAndAwait(
                UploadCompleteResponse(imageUrl = distributionUrl)
            )
        } catch (e: Exception) {
            ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValueAndAwait(
                ErrorResponse(
                    code = "FAILED_TO_COPY_IMAGE",
                    message = e.message ?: "Failed to complete upload"
                )
            )
        }
    }

    override suspend fun getMeals(request: ServerRequest): ServerResponse {
        // 認証済みユーザーのIDを取得
        val userId = getCurrentUserId()

        val input = GetMealsInput(
            userId = userId,
            startDate = null,
            endDate = null
        )

        return getMealsUseCase.execute(input).fold(
            ifLeft = { error -> error.toResponse() },
            ifRight = { output -> ServerResponse.ok().bodyValueAndAwait(output.toResponse()) }
        )
    }

    override suspend fun getMeal(request: ServerRequest): ServerResponse {
        // 認証済みユーザーのIDを取得
        val userId = getCurrentUserId()
        val mealId = UUID.fromString(request.pathVariable("mealId"))

        val input = GetMealInput(
            userId = userId,
            mealId = mealId
        )

        return getMealUseCase.execute(input).fold(
            ifLeft = { error -> error.toResponse() },
            ifRight = { output -> ServerResponse.ok().bodyValueAndAwait(output.toResponse()) }
        )
    }

    override suspend fun createMeal(request: ServerRequest): ServerResponse {
        val createMealRequest = request.awaitBody<CreateMealRequest>()

        // 認証済みユーザーのIDを取得
        val userId = getCurrentUserId()

        val input = PostMealInput(
            userId = userId,
            dishName = createMealRequest.dishName,
            cookedAt = createMealRequest.cookedAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
            memo = createMealRequest.memo,
            imageId = createMealRequest.imageId,
            recipeId = createMealRequest.recipeId
        )

        return postMealUseCase.execute(input).fold(
            ifLeft = { error -> error.toResponse() },
            ifRight = { output -> ServerResponse.ok().bodyValueAndAwait(output.toResponse()) }
        )
    }
}

// Extension functions for response conversion
fun GetUploadUrlOutput.toResponse(): UploadUrlResponse {
    return UploadUrlResponse(
        imageId = this.imageId,
        presignedUrl = this.presignedUrl
    )
}

fun PostMealOutput.toResponse(): MealResponse {
    return MealResponse(
        mealId = this.mealId,
        dishName = this.dishName,
        cookedAt = this.cookedAt.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        memo = this.memo,
        imageId = this.imageId,
        imageUrl = this.imageUrl,
        recipeId = this.recipeId
    )
}

fun GetMealsOutput.toResponse(): MealsResponse {
    return MealsResponse(
        meals = this.meals.map { it.toResponse() }
    )
}

fun GetMealOutput.toResponse(): MealResponse {
    return this.mealDto.toResponse()
}

fun MealDto.toResponse(): MealResponse {
    return MealResponse(
        mealId = this.mealId,
        dishName = this.dishName,
        cookedAt = this.cookedAt.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        memo = this.memo,
        imageId = this.imageId,
        imageUrl = this.imageUrl,
        recipeId = this.recipeId
    )
}
