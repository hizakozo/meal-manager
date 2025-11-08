package com.ken_stack.meal_manager_api.gateway

import com.ken_stack.meal_manager_api.domain.model.Image
import com.ken_stack.meal_manager_api.domain.model.Meal
import com.ken_stack.meal_manager_api.driver.ImageDbDriver
import com.ken_stack.meal_manager_api.driver.MealDbDriver
import com.ken_stack.meal_manager_api.driver.MealImageDbDriver
import com.ken_stack.meal_manager_api.driver.entities.ImageEntity
import com.ken_stack.meal_manager_api.driver.entities.MealEntity
import com.ken_stack.meal_manager_api.driver.entities.MealImageEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class MealRepositoryImplTest : StringSpec({

    val mealDbDriver = mockk<MealDbDriver>()
    val mealImageDbDriver = mockk<MealImageDbDriver>()
    val imageDbDriver = mockk<ImageDbDriver>()
    val mealRepository = MealRepositoryImpl(mealDbDriver, mealImageDbDriver, imageDbDriver)

    "画像なしの食事を保存できる" {
        // Given
        val meal = Meal.create(
            dishName = "カレーライス",
            cookedAt = LocalDateTime.of(2025, 11, 8, 12, 0),
            memo = "おいしかった",
            image = null,
            recipeId = null
        ).getOrNull()!!

        val savedEntity = MealEntity(
            mealId = meal.mealId.value,
            dishName = "カレーライス",
            cookedAt = LocalDateTime.of(2025, 11, 8, 12, 0),
            memo = "おいしかった"
        )

        every { mealDbDriver.save(any()) } returns Mono.just(savedEntity)
        every { mealDbDriver.findById(meal.mealId.value) } returns Mono.just(savedEntity)
        every { mealImageDbDriver.findById(meal.mealId.value) } returns Mono.empty()

        // When
        val savedMeal = mealRepository.save(meal)

        // Then
        savedMeal.dishName.value shouldBe "カレーライス"
        savedMeal.cookedAt.value shouldBe LocalDateTime.of(2025, 11, 8, 12, 0)
        savedMeal.memo.value shouldBe "おいしかった"
        savedMeal.image.shouldBeNull()

        coVerify { mealDbDriver.save(any()) }
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

        val savedMealEntity = MealEntity(
            mealId = meal.mealId.value,
            dishName = "パスタ",
            cookedAt = LocalDateTime.of(2025, 11, 8, 18, 30),
            memo = "トマトソース"
        )

        val savedImageEntity = ImageEntity(
            imageId = image.imageId,
            uploadedAt = image.uploadedAt
        )

        val savedMealImageEntity = MealImageEntity(
            mealId = meal.mealId.value,
            imageId = image.imageId
        )

        every { mealDbDriver.save(any()) } returns Mono.just(savedMealEntity)
        every { imageDbDriver.save(any()) } returns Mono.just(savedImageEntity)
        every { mealImageDbDriver.save(any()) } returns Mono.just(savedMealImageEntity)
        every { mealDbDriver.findById(meal.mealId.value) } returns Mono.just(savedMealEntity)
        every { mealImageDbDriver.findById(meal.mealId.value) } returns Mono.just(savedMealImageEntity)
        every { imageDbDriver.findById(image.imageId) } returns Mono.just(savedImageEntity)

        // When
        val savedMeal = mealRepository.save(meal)

        // Then
        savedMeal.dishName.value shouldBe "パスタ"
        savedMeal.image.shouldNotBeNull()
        savedMeal.image?.imageId shouldBe image.imageId

        coVerify { imageDbDriver.save(any()) }
        coVerify { mealImageDbDriver.save(any()) }
    }

    "存在しない食事IDで検索するとnullが返る" {
        // Given
        val nonExistentId = UUID.randomUUID()
        every { mealDbDriver.findById(nonExistentId) } returns Mono.empty()

        // When
        val result = mealRepository.findById(nonExistentId)

        // Then
        result.shouldBeNull()
    }

    "保存した食事を取得できる" {
        // Given
        val mealId = UUID.randomUUID()
        val mealEntity = MealEntity(
            mealId = mealId,
            dishName = "ラーメン",
            cookedAt = LocalDateTime.of(2025, 11, 8, 20, 0),
            memo = "醤油ラーメン"
        )

        every { mealDbDriver.findById(mealId) } returns Mono.just(mealEntity)
        every { mealImageDbDriver.findById(mealId) } returns Mono.empty()

        // When
        val foundMeal = mealRepository.findById(mealId)

        // Then
        foundMeal.shouldNotBeNull()
        foundMeal.dishName.value shouldBe "ラーメン"
        foundMeal.memo.value shouldBe "醤油ラーメン"
    }

    "すべての食事を取得できる" {
        // Given
        val meal1Entity = MealEntity(
            mealId = UUID.randomUUID(),
            dishName = "朝食",
            cookedAt = LocalDateTime.of(2025, 11, 8, 8, 0),
            memo = "トースト"
        )
        val meal2Entity = MealEntity(
            mealId = UUID.randomUUID(),
            dishName = "昼食",
            cookedAt = LocalDateTime.of(2025, 11, 8, 12, 0),
            memo = "弁当"
        )

        every { mealDbDriver.findAll() } returns Flux.just(meal1Entity, meal2Entity)
        every { mealImageDbDriver.findById(meal1Entity.mealId) } returns Mono.empty()
        every { mealImageDbDriver.findById(meal2Entity.mealId) } returns Mono.empty()

        // When
        val meals = mealRepository.findAll(null, null)

        // Then
        meals.shouldHaveSize(2)
    }

    "日付範囲で食事を絞り込みできる" {
        // Given
        val meal1Entity = MealEntity(
            mealId = UUID.randomUUID(),
            dishName = "11月7日の夕食",
            cookedAt = LocalDateTime.of(2025, 11, 7, 19, 0),
            memo = "昨日の夕食"
        )
        val meal2Entity = MealEntity(
            mealId = UUID.randomUUID(),
            dishName = "11月8日の夕食",
            cookedAt = LocalDateTime.of(2025, 11, 8, 19, 0),
            memo = "今日の夕食"
        )

        every { mealDbDriver.findAll() } returns Flux.just(meal1Entity, meal2Entity)
        every { mealImageDbDriver.findById(meal1Entity.mealId) } returns Mono.empty()
        every { mealImageDbDriver.findById(meal2Entity.mealId) } returns Mono.empty()

        // When
        val meals = mealRepository.findAll(
            startDate = LocalDate.of(2025, 11, 8),
            endDate = LocalDate.of(2025, 11, 8)
        )

        // Then
        meals.shouldHaveSize(1)
        meals[0].dishName.value shouldBe "11月8日の夕食"
    }
})
