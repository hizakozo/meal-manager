package com.ken_stack.meal_manager_api.gateway

import com.ken_stack.meal_manager_api.domain.service.ImagePresigner
import com.ken_stack.meal_manager_api.domain.service.ImageService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ImageServiceImpl(
    private val imagePresigner: ImagePresigner
) : ImageService {
    override suspend fun upload(imageId: UUID): String {
        return imagePresigner.generatePresignedUploadUrl(imageId)
    }

    override suspend fun getUrl(imageId: UUID): String {
        return imagePresigner.getImageUrl(imageId)
    }

    override suspend fun copyToDistribution(imageId: UUID) {
        imagePresigner.copyToDistribution(imageId)
    }
}
