package com.ken_stack.meal_manager_api.controller

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.HeadObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.net.url.Url
import com.ken_stack.meal_manager_api.controller.gen.model.CreateMealRequest
import com.ken_stack.meal_manager_api.controller.gen.model.MealResponse
import com.ken_stack.meal_manager_api.controller.gen.model.MealsResponse
import com.ken_stack.meal_manager_api.config.TestSecurityConfig
import com.ken_stack.meal_manager_api.controller.gen.model.UploadUrlResponse
import com.ken_stack.meal_manager_api.driver.ImageDbDriver
import com.ken_stack.meal_manager_api.driver.MealDbDriver
import com.ken_stack.meal_manager_api.driver.MealImageDbDriver
import com.ken_stack.meal_manager_api.driver.UserDbDriver
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@Import(TestSecurityConfig::class)
class MealApiIntegrationTest(
    private val context: ApplicationContext
) : StringSpec({

    lateinit var client: WebTestClient
    lateinit var mealDbDriver: MealDbDriver
    lateinit var mealImageDbDriver: MealImageDbDriver
    lateinit var imageDbDriver: ImageDbDriver
    lateinit var userDbDriver: UserDbDriver
    lateinit var s3Client: S3Client

    beforeTest {
        client = WebTestClient.bindToApplicationContext(context)
            .configureClient()
            .defaultHeader("Authorization", "Bearer test-token")
            .build()
        mealDbDriver = context.getBean(MealDbDriver::class.java)
        mealImageDbDriver = context.getBean(MealImageDbDriver::class.java)
        imageDbDriver = context.getBean(ImageDbDriver::class.java)
        userDbDriver = context.getBean(UserDbDriver::class.java)

        // S3クライアントの初期化
        val s3Endpoint = System.getProperty("AWS_S3_ENDPOINT")
        s3Client = S3Client {
            region = "us-east-1"
            endpointUrl = Url.parse(s3Endpoint)
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = "test"
                secretAccessKey = "test"
            }
            forcePathStyle = true
        }

        // テスト前にDBをクリーンアップ（外部キー制約の順序に注意）
        mealImageDbDriver.deleteAll().block()
        imageDbDriver.deleteAll().block()
        mealDbDriver.deleteAll().block()
        userDbDriver.deleteAll().block()
    }

    afterTest {
        s3Client.close()
    }

    "POST /meal-manager-api/meals - 画像なしで食事を作成できる" {
        // Given
        val request = CreateMealRequest(
            dishName = "カレーライス",
            cookedAt = OffsetDateTime.of(2025, 11, 9, 12, 0, 0, 0, ZoneOffset.UTC),
            memo = "スパイスから作った",
            imageId = null,
            recipeId = null
        )

        // When
        val response = client.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody<MealResponse>()
            .returnResult()
            .responseBody

        // Then - レスポンスの検証
        response shouldNotBe null
        response!!.mealId shouldNotBe null
        response.dishName shouldBe "カレーライス"
        response.memo shouldBe "スパイスから作った"
        response.imageId shouldBe null
        response.imageUrl shouldBe null
        response.recipeId shouldBe null

        // DBの検証
        val mealId = UUID.fromString(response.mealId.toString())
        val savedMealEntity = mealDbDriver.findById(mealId).awaitFirstOrNull()
        savedMealEntity shouldNotBe null
        savedMealEntity!!.dishName shouldBe "カレーライス"
        savedMealEntity.memo shouldBe "スパイスから作った"

        // meal_imagesテーブルにレコードがないことを確認
        val mealImageEntity = mealImageDbDriver.findById(mealId).awaitFirstOrNull()
        mealImageEntity shouldBe null
    }

    "POST /meal-manager-api/meals - 画像ありで食事を作成できる" {
        // Given - uploadバケットに画像をアップロード
        val imageId = UUID.randomUUID()
        val imageContent = "test image content".toByteArray()

        s3Client.putObject(PutObjectRequest {
            bucket = "meal-manager-upload"
            key = imageId.toString()
            body = ByteStream.fromBytes(imageContent)
        })

        // When - imageIdを含むリクエストでmealを作成
        val request = CreateMealRequest(
            dishName = "パスタ",
            cookedAt = OffsetDateTime.of(2025, 11, 9, 18, 30, 0, 0, ZoneOffset.UTC),
            memo = "トマトソース",
            imageId = imageId,
            recipeId = null
        )

        val response = client.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody<MealResponse>()
            .returnResult()
            .responseBody

        // Then - レスポンスの検証
        response shouldNotBe null
        response!!.mealId shouldNotBe null
        response.dishName shouldBe "パスタ"
        response.memo shouldBe "トマトソース"
        response.imageId shouldBe imageId
        response.imageUrl shouldNotBe null

        // DBの検証
        val mealId = UUID.fromString(response.mealId.toString())
        val savedMealEntity = mealDbDriver.findById(mealId).awaitFirstOrNull()
        savedMealEntity shouldNotBe null
        savedMealEntity!!.dishName shouldBe "パスタ"
        savedMealEntity.memo shouldBe "トマトソース"

        // meal_imagesテーブルにレコードが存在することを確認
        val mealImageEntity = mealImageDbDriver.findById(mealId).awaitFirstOrNull()
        mealImageEntity shouldNotBe null
        mealImageEntity!!.imageId shouldBe imageId

        // imagesテーブルにレコードが存在することを確認
        val imageEntity = imageDbDriver.findById(imageId).awaitFirstOrNull()
        imageEntity shouldNotBe null
        imageEntity!!.imageId shouldBe imageId

        // S3のdistributionバケットに画像がコピーされていることを確認
        val headObjectResponse = s3Client.headObject(HeadObjectRequest {
            bucket = "meal-manager-distribution"
            key = imageId.toString()
        })
        headObjectResponse shouldNotBe null
    }

    "GET /meal-manager-api/images/upload-url - アップロード用の署名付きURLを取得できる" {
        // When
        val response = client.get()
            .uri("/meal-manager-api/images/upload-url")
            .exchange()
            .expectStatus().isOk
            .expectBody<UploadUrlResponse>()
            .returnResult()
            .responseBody

        // Then - レスポンスの検証
        response shouldNotBe null
        response!!.imageId shouldNotBe null
        response.presignedUrl shouldNotBe null

        // presignedUrlが有効なURLであることを確認
        val presignedUrl = response.presignedUrl
        presignedUrl shouldNotBe null
        presignedUrl.contains("meal-manager-upload") shouldBe true
        presignedUrl.contains(response.imageId.toString()) shouldBe true

        // 署名付きURLを使って画像をアップロードできることを確認
        val imageContent = "test image content for upload".toByteArray()
        s3Client.putObject(PutObjectRequest {
            bucket = "meal-manager-upload"
            key = response.imageId.toString()
            body = ByteStream.fromBytes(imageContent)
        })

        // uploadバケットに画像が存在することを確認
        val headObjectResponse = s3Client.headObject(HeadObjectRequest {
            bucket = "meal-manager-upload"
            key = response.imageId.toString()
        })
        headObjectResponse shouldNotBe null
    }

    "GET /meal-manager-api/meals - 食事一覧を取得できる" {
        // Given - 複数の食事を作成
        val meal1ImageId = UUID.randomUUID()
        s3Client.putObject(PutObjectRequest {
            bucket = "meal-manager-upload"
            key = meal1ImageId.toString()
            body = ByteStream.fromBytes("image1".toByteArray())
        })

        val meal1Request = CreateMealRequest(
            dishName = "朝食",
            cookedAt = OffsetDateTime.of(2025, 11, 10, 8, 0, 0, 0, ZoneOffset.UTC),
            memo = "トースト",
            imageId = meal1ImageId,
            recipeId = null
        )
        client.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(meal1Request)
            .exchange()
            .expectStatus().isOk

        val meal2Request = CreateMealRequest(
            dishName = "昼食",
            cookedAt = OffsetDateTime.of(2025, 11, 10, 12, 0, 0, 0, ZoneOffset.UTC),
            memo = "弁当",
            imageId = null,
            recipeId = null
        )
        client.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(meal2Request)
            .exchange()
            .expectStatus().isOk

        // When - 食事一覧を取得
        val response = client.get()
            .uri("/meal-manager-api/meals")
            .exchange()
            .expectStatus().isOk
            .expectBody<MealsResponse>()
            .returnResult()
            .responseBody

        // Then
        response shouldNotBe null
        response!!.meals.size shouldBe 2
        response.meals.any { it.dishName == "朝食" } shouldBe true
        response.meals.any { it.dishName == "昼食" } shouldBe true
    }

    "GET /meal-manager-api/meals - 他のユーザーの食事は取得されない" {
        // Given - 別のユーザー（auth0|other-user）のクライアントを作成
        val otherUserClient = WebTestClient.bindToApplicationContext(context)
            .configureClient()
            .defaultHeader("Authorization", "Bearer other-user-token")
            .build()

        // 現在のユーザー（auth0|test-user-123）の食事を作成
        val currentUserMeal = CreateMealRequest(
            dishName = "現在ユーザーの食事",
            cookedAt = OffsetDateTime.of(2025, 11, 10, 12, 0, 0, 0, ZoneOffset.UTC),
            memo = "これは見えるべき",
            imageId = null,
            recipeId = null
        )
        client.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(currentUserMeal)
            .exchange()
            .expectStatus().isOk

        // 別ユーザーの食事を作成
        val otherUserMeal = CreateMealRequest(
            dishName = "他ユーザーの食事",
            cookedAt = OffsetDateTime.of(2025, 11, 10, 13, 0, 0, 0, ZoneOffset.UTC),
            memo = "これは見えないべき",
            imageId = null,
            recipeId = null
        )
        otherUserClient.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(otherUserMeal)
            .exchange()
            .expectStatus().isOk

        // When - 現在のユーザーとして食事一覧を取得
        val response = client.get()
            .uri("/meal-manager-api/meals")
            .exchange()
            .expectStatus().isOk
            .expectBody<MealsResponse>()
            .returnResult()
            .responseBody

        // Then - 現在のユーザーの食事のみが取得される
        response shouldNotBe null
        response!!.meals.size shouldBe 1
        response.meals[0].dishName shouldBe "現在ユーザーの食事"
        response.meals.any { it.dishName == "他ユーザーの食事" } shouldBe false
    }

    "POST /meal-manager-api/meals - バリデーションエラー（dishNameが空）" {
        // Given
        val request = CreateMealRequest(
            dishName = "",
            cookedAt = OffsetDateTime.of(2025, 11, 9, 12, 0, 0, 0, ZoneOffset.UTC),
            memo = "メモ",
            imageId = null,
            recipeId = null
        )

        // When & Then
        client.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }

    "POST /meal-manager-api/meals - バリデーションエラー（memoが1000文字を超える）" {
        // Given
        val request = CreateMealRequest(
            dishName = "カレーライス",
            cookedAt = OffsetDateTime.of(2025, 11, 9, 12, 0, 0, 0, ZoneOffset.UTC),
            memo = "a".repeat(1001), // 1001文字
            imageId = null,
            recipeId = null
        )

        // When & Then
        client.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
    }

    "GET /meal-manager-api/meals/{mealId} - 自分の食事を取得できる" {
        // Given - 食事を作成
        val createRequest = CreateMealRequest(
            dishName = "詳細取得テスト",
            cookedAt = OffsetDateTime.of(2025, 11, 10, 12, 0, 0, 0, ZoneOffset.UTC),
            memo = "詳細取得用の食事",
            imageId = null,
            recipeId = null
        )
        val createResponse = client.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(createRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody<MealResponse>()
            .returnResult()
            .responseBody

        val mealId = createResponse!!.mealId

        // When - 食事詳細を取得
        val response = client.get()
            .uri("/meal-manager-api/meals/$mealId")
            .exchange()
            .expectStatus().isOk
            .expectBody<MealResponse>()
            .returnResult()
            .responseBody

        // Then
        response shouldNotBe null
        response!!.mealId shouldBe mealId
        response.dishName shouldBe "詳細取得テスト"
        response.memo shouldBe "詳細取得用の食事"
    }

    "GET /meal-manager-api/meals/{mealId} - 他人の食事は取得できない（403エラー）" {
        // Given - 別ユーザーのクライアントを作成
        val otherUserClient = WebTestClient.bindToApplicationContext(context)
            .configureClient()
            .defaultHeader("Authorization", "Bearer other-user-token")
            .build()

        // 別ユーザーの食事を作成
        val otherUserMeal = CreateMealRequest(
            dishName = "他人の食事",
            cookedAt = OffsetDateTime.of(2025, 11, 10, 12, 0, 0, 0, ZoneOffset.UTC),
            memo = "これは見えないはず",
            imageId = null,
            recipeId = null
        )
        val otherUserResponse = otherUserClient.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(otherUserMeal)
            .exchange()
            .expectStatus().isOk
            .expectBody<MealResponse>()
            .returnResult()
            .responseBody

        val otherUserMealId = otherUserResponse!!.mealId

        // When - 現在のユーザーとして他人の食事を取得しようとする
        client.get()
            .uri("/meal-manager-api/meals/$otherUserMealId")
            .exchange()
            .expectStatus().isForbidden
    }

    "GET /meal-manager-api/meals/{mealId} - 存在しない食事IDでは404エラー" {
        // Given - 存在しないmealId
        val nonExistentMealId = UUID.randomUUID()

        // When & Then
        client.get()
            .uri("/meal-manager-api/meals/$nonExistentMealId")
            .exchange()
            .expectStatus().isNotFound
    }

    "GET /meal-manager-api/meals/{mealId} - 画像ありの食事を取得できる" {
        // Given - 画像をアップロード
        val imageId = UUID.randomUUID()
        val imageContent = "test image for detail".toByteArray()

        s3Client.putObject(PutObjectRequest {
            bucket = "meal-manager-upload"
            key = imageId.toString()
            body = ByteStream.fromBytes(imageContent)
        })

        // 画像ありの食事を作成
        val createRequest = CreateMealRequest(
            dishName = "画像付き食事",
            cookedAt = OffsetDateTime.of(2025, 11, 10, 18, 0, 0, 0, ZoneOffset.UTC),
            memo = "画像付きテスト",
            imageId = imageId,
            recipeId = null
        )
        val createResponse = client.post()
            .uri("/meal-manager-api/meals")
            .bodyValue(createRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody<MealResponse>()
            .returnResult()
            .responseBody

        val mealId = createResponse!!.mealId

        // When - 食事詳細を取得
        val response = client.get()
            .uri("/meal-manager-api/meals/$mealId")
            .exchange()
            .expectStatus().isOk
            .expectBody<MealResponse>()
            .returnResult()
            .responseBody

        // Then
        response shouldNotBe null
        response!!.mealId shouldBe mealId
        response.dishName shouldBe "画像付き食事"
        response.imageId shouldBe imageId
        response.imageUrl shouldNotBe null
    }
})
