package com.ken_stack.meal_manager_api.domain.service

import java.util.UUID

interface ImageService {
    suspend fun upload(imageId: UUID): String
    suspend fun getUrl(imageId: UUID): String
    suspend fun copyToDistribution(imageId: UUID)
}
