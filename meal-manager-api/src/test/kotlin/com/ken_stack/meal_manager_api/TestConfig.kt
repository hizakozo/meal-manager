package com.ken_stack.meal_manager_api

import io.kotest.core.config.AbstractProjectConfig
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.resource.ClassLoaderResourceAccessor
import org.springframework.core.io.ClassPathResource
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.DriverManager

class TestConfig : AbstractProjectConfig() {
    override suspend fun beforeProject() {
        // テストコンテナーの起動
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
    }
}
