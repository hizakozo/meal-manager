package com.ken_stack.meal_manager_api.gateway

import com.ken_stack.meal_manager_api.domain.service.ImagePresigner
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.util.UUID

@TestConfiguration
class TestImagePresignerConfig {

    @Bean
    fun imagePresigner(): ImagePresigner {
        return object : ImagePresigner {
            override suspend fun generatePresignedUploadUrl(imageId: UUID): String {
                return "http://localhost:4566/meal-manager-upload/$imageId"
            }

            override suspend fun getImageUrl(imageId: UUID): String {
                return "http://localhost:4566/meal-manager-distribution/$imageId"
            }

            override suspend fun copyToDistribution(imageId: UUID) {
                // テストではダミー実装
            }
        }
    }
}
