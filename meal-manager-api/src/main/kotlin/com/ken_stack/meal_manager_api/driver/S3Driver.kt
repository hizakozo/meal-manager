package com.ken_stack.meal_manager_api.driver

import aws.sdk.kotlin.runtime.auth.credentials.DefaultChainCredentialsProvider
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.CopyObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignPutObject
import aws.smithy.kotlin.runtime.net.url.Url
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.time.Duration.Companion.hours

@Component
class S3Driver(
    @Value("\${aws.s3.endpoint:}") private val endpoint: String?,
    @Value("\${aws.s3.upload-bucket}") private val uploadBucket: String,
    @Value("\${aws.s3.distribution-bucket}") private val distributionBucket: String,
    @Value("\${aws.region}") private val region: String,
    @Value("\${aws.s3.use-local:false}") private val useLocal: Boolean
) {

    private val s3Client = S3Client {
        this.region = this@S3Driver.region

        if (useLocal && !endpoint.isNullOrEmpty()) {
            endpointUrl = Url.parse(endpoint)
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = "test"
                secretAccessKey = "test"
            }
            forcePathStyle = true
        } else {
            credentialsProvider = DefaultChainCredentialsProvider()
        }
    }

    suspend fun generatePresignedUploadUrl(imageId: UUID): String {
        val request = PutObjectRequest {
            bucket = uploadBucket
            key = imageId.toString()
        }

        val presignedRequest = s3Client.presignPutObject(request, 1.hours)
        return presignedRequest.url.toString()
    }

    suspend fun copyToDistribution(imageId: UUID) {
        val copyRequest = CopyObjectRequest {
            copySource = "$uploadBucket/$imageId"
            bucket = distributionBucket
            key = imageId.toString()
        }

        s3Client.copyObject(copyRequest)
    }
}
