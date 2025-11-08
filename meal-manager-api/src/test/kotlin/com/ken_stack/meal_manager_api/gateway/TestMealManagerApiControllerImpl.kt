package com.ken_stack.meal_manager_api.gateway

import com.ken_stack.meal_manager_api.controller.gen.api.IMealManagerApiController
import com.ken_stack.meal_manager_api.controller.gen.model.UploadUrlResponse
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@TestConfiguration
class TestMealManagerApiControllerImpl {

    @Bean
    fun iMealManagerApiController(): IMealManagerApiController {
        return object : IMealManagerApiController {
            override fun getUploadUrl(exchange: ServerWebExchange): Mono<UploadUrlResponse> {
                return Mono.empty()
            }
        }
    }
}
