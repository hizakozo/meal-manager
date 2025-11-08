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
        val (imageId, presignedUrl) = try {
            imageStore.getUploadPresignedUrl()
        } catch (e: Exception) {
            raise(GetUploadUrlUseCaseErrors.FailedToGeneratePresignedUrl(e.message ?: "Unknown error"))
        }

        GetUploadUrlOutput(
            imageId = imageId,
            presignedUrl = presignedUrl
        )
    }
}

interface GetUploadUrlUseCaseError

sealed class GetUploadUrlUseCaseErrors : GetUploadUrlUseCaseError {
    data class FailedToGeneratePresignedUrl(val message: String) : GetUploadUrlUseCaseErrors()
}

data class GetUploadUrlOutput(
    val imageId: UUID,
    val presignedUrl: String
)
