package com.ken_stack.meal_manager_api.driver

import com.ken_stack.meal_manager_api.gateway.ImagePresigner
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Profile("local")
class LocalStackImagePresigner(
    @Value("\${aws.s3.endpoint}") private val endpoint: String,
    @Value("\${aws.s3.distribution-bucket}") private val distributionBucket: String
) : ImagePresigner {

    override suspend fun getDistributionUrl(imageId: UUID): String {
        return "$endpoint/$distributionBucket/$imageId"
    }
}
