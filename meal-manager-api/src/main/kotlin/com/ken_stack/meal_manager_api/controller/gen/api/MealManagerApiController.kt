package com.ken_stack.meal_manager_api.controller.gen.api

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

interface IMealManagerApiController {
        suspend fun completeUpload(request: ServerRequest): ServerResponse
        suspend fun getUploadUrl(request: ServerRequest): ServerResponse
}

@Configuration
class MealManagerApiControllerRouter {
@Bean
fun mealManagerApiControllerRoutes(controller: IMealManagerApiController) = coRouter {
        POST("/meal-manager-api/images/{imageId}/upload/complete", controller::completeUpload)
        GET("/meal-manager-api/images/upload-url", controller::getUploadUrl)
    }
}