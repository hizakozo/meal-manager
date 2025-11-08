package com.ken_stack.meal_manager_api.gateway

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.util.UUID

@TestConfiguration
class TestImagePresignerConfig {

    @Bean
    fun imagePresigner(): ImagePresigner {
        return object : ImagePresigner {
            override suspend fun getDistributionUrl(imageId: UUID): String {
                return "http://localhost:4566/meal-manager-distribution/$imageId"
            }
        }
    }
}
