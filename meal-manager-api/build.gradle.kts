plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.13.0"
	id("org.liquibase.gradle") version "2.2.0"
}

group = "com.ken-stack"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// R2DBC for reactive database access
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:r2dbc-postgresql")

	// Spring Security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.security:spring-security-oauth2-resource-server")
	implementation("org.springframework.security:spring-security-oauth2-jose")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:r2dbc")
	testImplementation("org.testcontainers:localstack")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Kotest
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")

	// Mockk
	testImplementation("io.mockk:mockk:1.13.13")
	testImplementation("com.ninja-squad:springmockk:4.0.2")

	// Arrow kt
	implementation("io.arrow-kt:arrow-core:2.1.0")
	implementation("io.arrow-kt:arrow-fx-coroutines:2.1.0")

	// AWS SDK
	implementation(platform("aws.sdk.kotlin:bom:1.3.93"))
	implementation("aws.sdk.kotlin:s3")

	// Liquibase
	implementation("org.liquibase:liquibase-core:4.19.0")
	implementation("org.liquibase:liquibase-groovy-dsl:3.0.2")
	liquibaseRuntime("org.postgresql:postgresql:42.3.1")
	liquibaseRuntime("org.liquibase:liquibase-core:4.19.0")
	liquibaseRuntime("org.liquibase:liquibase-groovy-dsl:3.0.2")
	liquibaseRuntime("info.picocli:picocli:4.6.1")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

openApiGenerate {
	generatorName.set("kotlin-spring")
	inputSpec.set("$rootDir/../docs/openapi.yaml")

	outputDir.set("$rootDir")
	globalProperties.set(
		mapOf(
			"models" to "",
			"apis" to "",
			"supportingFiles" to "true"
		)
	)

	additionalProperties.set(
		mapOf(
			"modelPackage" to "com.ken_stack.meal_manager_api.controller.gen.model",
			"apiPackage" to "com.ken_stack.meal_manager_api.controller.gen.api",
			"reactive" to "true",
			"useBeanValidation" to "false",
			"documentationProvider" to "none",
			"interfaceOnly" to "true",
			"apiSuffix" to "Controller",
		)
	)

	templateDir.set("$rootDir/../docs/templates/kotlin-spring")
}

liquibase {
	activities.register("main") {
		val db_url = System.getenv("DB_URL") ?: "localhost:5433/meal-manager"
		val db_user = System.getenv("DB_USER") ?: "docker"
		val db_password = System.getenv("DB_PASS") ?: "docker"

		this.arguments = mapOf(
			"logLevel" to "info",
			"changeLogFile" to "src/main/resources/liquibase/xml/db.changelog.xml",
			"url" to "jdbc:postgresql://$db_url",
			"username" to db_user,
			"password" to db_password,
		)
	}
	runList = "main"
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
	builder.set("paketobuildpacks/builder:base")
	imageName.set("${project.group}/${project.name}:${project.version}")

	// ARM64アーキテクチャを指定
	environment.set(mapOf(
		"BP_JVM_VERSION" to "21",
		"BP_ARCH" to "arm64"
	))
}
