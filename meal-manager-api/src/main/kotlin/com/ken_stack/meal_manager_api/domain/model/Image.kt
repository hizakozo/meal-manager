package com.ken_stack.meal_manager_api.domain.model

import arrow.core.Either
import arrow.core.raise.either
import com.ken_stack.meal_manager_api.domain.DomainError
import java.time.LocalDateTime
import java.util.UUID

class Image private constructor(
    val imageId: UUID,
    val uploadedAt: LocalDateTime
) {
    companion object {
        fun create(): Either<DomainError, Image> = either {
            Image(
                imageId = UUID.randomUUID(),
                uploadedAt = LocalDateTime.now()
            )
        }

        fun of(imageId: UUID, uploadedAt: LocalDateTime): Image {
            return Image(imageId, uploadedAt)
        }
    }
}
