package com.ken_stack.meal_manager_api.controller.gen.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 
 * @param imageId 画像の一意識別子
 * @param presignedUrl S3署名付きURL
 */
data class UploadUrlResponse(

    @get:JsonProperty("imageId", required = true) val imageId: java.util.UUID,

    @get:JsonProperty("presignedUrl", required = true) val presignedUrl: kotlin.String
    ) {

}

