package com.ken_stack.meal_manager_api.gateway

import com.ken_stack.meal_manager_api.domain.model.Image
import com.ken_stack.meal_manager_api.domain.model.Meal
import com.ken_stack.meal_manager_api.domain.repository.MealRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.r2dbc.core.DatabaseClient
import java.time.LocalDateTime

@SpringBootTest
@Import(TestImagePresignerConfig::class, TestMealManagerApiControllerImpl::class)
class MealRepositoryImplTest(
    private val mealRepository: MealRepository,
    private val databaseClient: DatabaseClient
) : StringSpec({

    beforeTest {
        // テストデータをクリーンアップ
        databaseClient.sql("DELETE FROM meal_images").fetch().rowsUpdated().block()
        databaseClient.sql("DELETE FROM meals").fetch().rowsUpdated().block()
        databaseClient.sql("DELETE FROM images").fetch().rowsUpdated().block()
    }

    "画像なしの食事を保存できる" {
        // Given
        val meal = Meal.create(
            dishName = "カレーライス",
            cookedAt = LocalDateTime.of(2025, 11, 8, 12, 0),
            memo = "おいしかった",
            image = null,
            recipeId = null
        ).getOrNull()!!

        // When
        val savedMeal = mealRepository.save(meal)

        // Then
        savedMeal.dishName.value shouldBe "カレーライス"
        savedMeal.cookedAt.value shouldBe LocalDateTime.of(2025, 11, 8, 12, 0)
        savedMeal.memo.value shouldBe "おいしかった"
        savedMeal.image.shouldBeNull()

        // DBに保存されたか確認
        val count = databaseClient.sql("SELECT count(*) FROM meals WHERE dish_name = 'カレーライス'")
            .map { row -> row.get(0, Long::class.java) }
            .first()
            .block()
        count shouldBe 1L
    }

    "画像ありの食事を保存できる" {
        // Given
        val image = Image.create().getOrNull()!!
        val meal = Meal.create(
            dishName = "パスタ",
            cookedAt = LocalDateTime.of(2025, 11, 8, 18, 30),
            memo = "トマトソース",
            image = image,
            recipeId = null
        ).getOrNull()!!

        // When
        val savedMeal = mealRepository.save(meal)

        // Then
        savedMeal.dishName.value shouldBe "パスタ"
        savedMeal.image.shouldNotBeNull()
        savedMeal.image?.imageId shouldBe image.imageId

        // DBに保存されたか確認
        val mealCount = databaseClient.sql("SELECT count(*) FROM meals WHERE dish_name = 'パスタ'")
            .map { row -> row.get(0, Long::class.java) }
            .first()
            .block()
        mealCount shouldBe 1L

        val imageCount = databaseClient.sql("SELECT count(*) FROM images WHERE image_id = '${image.imageId}'")
            .map { row -> row.get(0, Long::class.java) }
            .first()
            .block()
        imageCount shouldBe 1L
    }

    "存在しない食事IDで検索するとnullが返る" {
        // Given
        val nonExistentId = java.util.UUID.randomUUID()

        // When
        val result = mealRepository.findById(nonExistentId)

        // Then
        result.shouldBeNull()
    }

    "保存した食事を取得できる" {
        // Given
        val meal = Meal.create(
            dishName = "ラーメン",
            cookedAt = LocalDateTime.of(2025, 11, 8, 20, 0),
            memo = "醤油ラーメン",
            image = null,
            recipeId = null
        ).getOrNull()!!
        val savedMeal = mealRepository.save(meal)

        // When
        val foundMeal = mealRepository.findById(savedMeal.mealId.value)

        // Then
        foundMeal.shouldNotBeNull()
        foundMeal.dishName.value shouldBe "ラーメン"
        foundMeal.memo.value shouldBe "醤油ラーメン"
    }

    "すべての食事を取得できる" {
        // Given
        val meal1 = Meal.create(
            dishName = "朝食",
            cookedAt = LocalDateTime.of(2025, 11, 8, 8, 0),
            memo = "トースト",
            image = null,
            recipeId = null
        ).getOrNull()!!
        mealRepository.save(meal1)

        val meal2 = Meal.create(
            dishName = "昼食",
            cookedAt = LocalDateTime.of(2025, 11, 8, 12, 0),
            memo = "弁当",
            image = null,
            recipeId = null
        ).getOrNull()!!
        mealRepository.save(meal2)

        // When
        val meals = mealRepository.findAll(null, null)

        // Then
        meals.shouldHaveSize(2)
    }

    "日付範囲で食事を絞り込みできる" {
        // Given
        val meal1 = Meal.create(
            dishName = "11月7日の夕食",
            cookedAt = LocalDateTime.of(2025, 11, 7, 19, 0),
            memo = "昨日の夕食",
            image = null,
            recipeId = null
        ).getOrNull()!!
        mealRepository.save(meal1)

        val meal2 = Meal.create(
            dishName = "11月8日の夕食",
            cookedAt = LocalDateTime.of(2025, 11, 8, 19, 0),
            memo = "今日の夕食",
            image = null,
            recipeId = null
        ).getOrNull()!!
        mealRepository.save(meal2)

        // When
        val meals = mealRepository.findAll(
            startDate = java.time.LocalDate.of(2025, 11, 8),
            endDate = java.time.LocalDate.of(2025, 11, 8)
        )

        // Then
        meals.shouldHaveSize(1)
        meals[0].dishName.value shouldBe "11月8日の夕食"
    }
})
