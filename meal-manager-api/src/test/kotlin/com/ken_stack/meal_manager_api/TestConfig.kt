package com.ken_stack.meal_manager_api

import io.kotest.core.config.AbstractProjectConfig
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.resource.ClassLoaderResourceAccessor
import org.springframework.core.io.ClassPathResource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager

class TestConfig : AbstractProjectConfig() {
    override suspend fun beforeProject() {
        // PostgreSQLコンテナの起動
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:14.5")
        postgresContainer.start()

        System.setProperty("DB_USER", postgresContainer.username)
        System.setProperty("DB_PASSWORD", postgresContainer.password)
        System.setProperty("DB_NAME", postgresContainer.databaseName)
        System.setProperty("DB_PORT", postgresContainer.firstMappedPort.toString())

        val connection = DriverManager.getConnection(
            postgresContainer.jdbcUrl,
            postgresContainer.username,
            postgresContainer.password
        )

        // Liquibaseの初期化
        Liquibase(
            ClassPathResource("liquibase/xml/db.changelog.xml").path,
            ClassLoaderResourceAccessor(),
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                liquibase.database.jvm.JdbcConnection(
                    connection
                )
            )
        ).update("")

        // LocalStackコンテナの起動
        val localStackContainer = LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.S3)
        localStackContainer.start()

        val s3Endpoint = localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString()
        System.setProperty("AWS_S3_ENDPOINT", s3Endpoint)

        // S3バケットの作成
        val s3Client = aws.sdk.kotlin.services.s3.S3Client {
            region = "us-east-1"
            endpointUrl = aws.smithy.kotlin.runtime.net.url.Url.parse(s3Endpoint)
            credentialsProvider = aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider {
                accessKeyId = "test"
                secretAccessKey = "test"
            }
            forcePathStyle = true
        }

        kotlinx.coroutines.runBlocking {
            s3Client.createBucket(aws.sdk.kotlin.services.s3.model.CreateBucketRequest {
                bucket = "meal-manager-upload"
            })
            s3Client.createBucket(aws.sdk.kotlin.services.s3.model.CreateBucketRequest {
                bucket = "meal-manager-distribution"
            })
            s3Client.close()
        }
    }
}
