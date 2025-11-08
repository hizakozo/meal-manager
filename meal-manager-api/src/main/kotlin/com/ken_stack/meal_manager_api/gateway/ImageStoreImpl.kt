package com.ken_stack.meal_manager_api.gateway

import com.ken_stack.meal_manager_api.domain.service.ImageStore
import com.ken_stack.meal_manager_api.driver.S3Driver
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ImageStoreImpl(
    private val s3Driver: S3Driver,
    private val imagePresigner: ImagePresigner
) : ImageStore {

    override suspend fun getUploadPresignedUrl(): Pair<UUID, String> {
        val imageId = UUID.randomUUID()
        val presignedUrl = s3Driver.generatePresignedUploadUrl(imageId)
        return Pair(imageId, presignedUrl)
    }

    override suspend fun copyToDistribution(imageId: UUID) {
        s3Driver.copyToDistribution(imageId)
    }

    override suspend fun getDistributionUrl(imageId: UUID): String {
        return imagePresigner.getDistributionUrl(imageId)
    }
}
