package com.ken_stack.meal_manager_api.domain.service

import java.util.UUID

interface ImagePresigner {
    suspend fun generatePresignedUploadUrl(imageId: UUID): String
    suspend fun getImageUrl(imageId: UUID): String
    suspend fun copyToDistribution(imageId: UUID)
}
