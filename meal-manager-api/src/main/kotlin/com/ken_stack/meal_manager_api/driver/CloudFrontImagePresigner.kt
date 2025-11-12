package com.ken_stack.meal_manager_api.driver

import com.ken_stack.meal_manager_api.gateway.ImagePresigner
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Profile("dev", "stg", "prod")
class CloudFrontImagePresigner(
    @Value("\${aws.cloudfront.distribution-domain}") private val distributionDomain: String
) : ImagePresigner {

    override suspend fun getDistributionUrl(imageId: UUID): String {
        return "https://$distributionDomain/$imageId"
    }
}
