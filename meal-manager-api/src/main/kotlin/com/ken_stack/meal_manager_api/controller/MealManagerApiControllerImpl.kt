package com.ken_stack.meal_manager_api.controller

import com.ken_stack.meal_manager_api.controller.gen.api.IMealManagerApiController
import com.ken_stack.meal_manager_api.controller.gen.model.CreateMealRequest
import com.ken_stack.meal_manager_api.controller.gen.model.MealResponse
import com.ken_stack.meal_manager_api.controller.gen.model.UploadCompleteResponse
import com.ken_stack.meal_manager_api.controller.gen.model.UploadUrlResponse
import com.ken_stack.meal_manager_api.domain.service.ImageStore
import com.ken_stack.meal_manager_api.useCase.GetUploadUrlOutput
import com.ken_stack.meal_manager_api.useCase.GetUploadUrlUseCase
import com.ken_stack.meal_manager_api.useCase.PostMealInput
import com.ken_stack.meal_manager_api.useCase.PostMealOutput
import com.ken_stack.meal_manager_api.useCase.PostMealUseCase
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
    private val imageStore: ImageStore
) : IMealManagerApiController {

    override suspend fun getUploadUrl(request: ServerRequest): ServerResponse {
        return getUploadUrlUseCase.execute().fold(
            ifLeft = { error ->
                ServerResponse.badRequest().bodyValueAndAwait(mapOf("error" to error.toString()))
            },
            ifRight = { output ->
                ServerResponse.ok().bodyValueAndAwait(output.toResponse())
            }
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
            ServerResponse.badRequest().bodyValueAndAwait(
                mapOf("error" to (e.message ?: "Failed to complete upload"))
            )
        }
    }

    override suspend fun createMeal(request: ServerRequest): ServerResponse {
        val createMealRequest = request.awaitBody<CreateMealRequest>()

        val input = PostMealInput(
            dishName = createMealRequest.dishName,
            cookedAt = createMealRequest.cookedAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
            memo = createMealRequest.memo,
            imageId = createMealRequest.imageId,
            recipeId = createMealRequest.recipeId
        )

        return postMealUseCase.execute(input).fold(
            ifLeft = { error ->
                ServerResponse.badRequest().bodyValueAndAwait(
                    mapOf("error" to error.message)
                )
            },
            ifRight = { output ->
                ServerResponse.ok().bodyValueAndAwait(output.toResponse())
            }
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
