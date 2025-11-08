package com.ken_stack.meal_manager_api.useCase

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.service.ImageStore
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetUploadUrlUseCase(
    private val imageStore: ImageStore
) {
    suspend fun execute(): Either<GetUploadUrlUseCaseError, GetUploadUrlOutput> = either {
        val (imageId, presignedUrl) = imageStore.getUploadPresignedUrl()

        GetUploadUrlOutput(
            imageId = imageId,
            presignedUrl = presignedUrl
        )
    }
}

sealed class GetUploadUrlUseCaseError(override val code: ErrorCode, override val message: String) : UseCaseError

data class GetUploadUrlOutput(
    val imageId: UUID,
    val presignedUrl: String
)
