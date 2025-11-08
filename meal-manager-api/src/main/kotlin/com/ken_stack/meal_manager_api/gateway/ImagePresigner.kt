package com.ken_stack.meal_manager_api.gateway

import java.util.UUID

interface ImagePresigner {
    suspend fun getDistributionUrl(imageId: UUID): String
}
