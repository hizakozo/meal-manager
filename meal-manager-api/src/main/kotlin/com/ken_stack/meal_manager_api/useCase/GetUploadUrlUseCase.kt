package com.ken_stack.meal_manager_api.useCase

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.service.ImageService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetUploadUrlUseCase(
    private val imageService: ImageService
) {
    suspend fun execute(): Either<GetUploadUrlUseCaseError, GetUploadUrlOutput> = either {
        val imageId = UUID.randomUUID()
        val presignedUrl = try {
            imageService.upload(imageId)
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
