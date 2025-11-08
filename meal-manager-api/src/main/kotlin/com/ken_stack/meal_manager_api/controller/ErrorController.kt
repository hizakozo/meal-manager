package com.ken_stack.meal_manager_api.controller

import com.ken_stack.meal_manager_api.useCase.ErrorCode
import com.ken_stack.meal_manager_api.useCase.UseCaseError
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait

object ErrorController {
    suspend fun UseCaseError.toResponse(): ServerResponse {
        val errorResponse = ErrorResponse(
            code = this.code.name,
            message = this.message
        )

        val status = when (this.code) {
            ErrorCode.VALIDATION_ERROR -> HttpStatus.BAD_REQUEST
            ErrorCode.REPOSITORY_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
            ErrorCode.FAILED_TO_COPY_IMAGE -> HttpStatus.INTERNAL_SERVER_ERROR
            ErrorCode.FAILED_TO_GENERATE_PRESIGNED_URL -> HttpStatus.INTERNAL_SERVER_ERROR
            ErrorCode.MEAL_NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorCode.FORBIDDEN -> HttpStatus.FORBIDDEN
        }

        return ServerResponse.status(status).bodyValueAndAwait(errorResponse)
    }
}

data class ErrorResponse(
    val code: String,
    val message: String
)