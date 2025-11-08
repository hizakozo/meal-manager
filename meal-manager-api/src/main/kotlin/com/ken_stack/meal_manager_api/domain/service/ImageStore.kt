package com.ken_stack.meal_manager_api.domain.service

import java.util.UUID

interface ImageStore {
    suspend fun getUploadPresignedUrl(): Pair<UUID, String>
    suspend fun copyToDistribution(imageId: UUID)
    suspend fun getDistributionUrl(imageId: UUID): String
}
