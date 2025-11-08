package com.ken_stack.meal_manager_api.useCase

interface UseCaseError {
    val code: ErrorCode
    val message: String
}

enum class ErrorCode(val code: String) {
    VALIDATION_ERROR("VALIDATION_ERROR"),
    FAILED_TO_COPY_IMAGE("FAILED_TO_COPY_IMAGE"),
    REPOSITORY_ERROR("REPOSITORY_ERROR"),
    FAILED_TO_GENERATE_PRESIGNED_URL("FAILED_TO_GENERATE_PRESIGNED_URL")
}