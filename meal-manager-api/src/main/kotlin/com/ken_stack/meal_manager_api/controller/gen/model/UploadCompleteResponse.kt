package com.ken_stack.meal_manager_api.controller.gen.model

import java.util.Objects
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 
 * @param imageUrl CloudFront配信URL
 */
data class UploadCompleteResponse(

    @get:JsonProperty("imageUrl", required = true) val imageUrl: kotlin.String
    ) {

}

