package com.ken_stack.meal_manager_api.useCase

import com.ken_stack.meal_manager_api.domain.model.Image
import com.ken_stack.meal_manager_api.domain.model.Meal
import com.ken_stack.meal_manager_api.domain.model.UserId
import com.ken_stack.meal_manager_api.domain.repository.MealRepository
import com.ken_stack.meal_manager_api.domain.service.ImageStore
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.UUID

class GetMealUseCaseTest : StringSpec({

    val mealRepository = mockk<MealRepository>()
    val imageStore = mockk<ImageStore>()
    val useCase = GetMealUseCase(mealRepository, imageStore)

    "正常系: 自分の食事を取得できる" {
        // Given
        val userId = UserId.generate()
        val mealId = UUID.randomUUID()
        val meal = Meal.of(
            mealId = mealId,
            userId = userId,
            dishName = "カレーライス",
            cookedAt = LocalDateTime.of(2025, 11, 9, 12, 0),
            memo = "おいしかった",
            image = null,
            recipeId = null
        )

        coEvery { mealRepository.findById(mealId) } returns meal

        val input = GetMealInput(
            userId = userId,
            mealId = mealId
        )

        // When
        val result = useCase.execute(input)

        // Then
        result.isRight() shouldBe true
        result.getOrNull()!!.mealDto.apply {
            this.mealId shouldBe mealId
            dishName shouldBe "カレーライス"
            memo shouldBe "おいしかった"
            imageId shouldBe null
            imageUrl shouldBe null
        }
    }

    "正常系: 画像ありの自分の食事を取得できる" {
        // Given
        val userId = UserId.generate()
        val mealId = UUID.randomUUID()
        val image = Image.create().getOrNull()!!
        val meal = Meal.of(
            mealId = mealId,
            userId = userId,
            dishName = "パスタ",
            cookedAt = LocalDateTime.of(2025, 11, 9, 18, 30),
            memo = "トマトソース",
            image = image,
            recipeId = null
        )

        val distributionUrl = "https://cloudfront.example.com/${image.imageId}"

        coEvery { mealRepository.findById(mealId) } returns meal
        coEvery { imageStore.getDistributionUrl(image.imageId) } returns distributionUrl

        val input = GetMealInput(
            userId = userId,
            mealId = mealId
        )

        // When
        val result = useCase.execute(input)

        // Then
        result.isRight() shouldBe true
        result.getOrNull()!!.mealDto.apply {
            this.mealId shouldBe mealId
            dishName shouldBe "パスタ"
            memo shouldBe "トマトソース"
            imageId shouldBe image.imageId
            imageUrl shouldBe distributionUrl
        }
    }

    "異常系: 存在しないmealIdだと404エラー" {
        // Given
        val userId = UserId.generate()
        val nonExistentMealId = UUID.randomUUID()

        coEvery { mealRepository.findById(nonExistentMealId) } returns null

        val input = GetMealInput(
            userId = userId,
            mealId = nonExistentMealId
        )

        // When
        val result = useCase.execute(input)

        // Then
        result.isLeft() shouldBe true
        val error = result.leftOrNull()!!
        error.shouldBeInstanceOf<GetMealUseCaseErrors.MealNotFound>()
        error.code shouldBe ErrorCode.MEAL_NOT_FOUND
    }

    "異常系: 他人の食事を取得しようとすると403エラー" {
        // Given
        val userId = UserId.generate()
        val otherUserId = UserId.generate()
        val mealId = UUID.randomUUID()

        // 他人の食事
        val otherUserMeal = Meal.of(
            mealId = mealId,
            userId = otherUserId,
            dishName = "他人の食事",
            cookedAt = LocalDateTime.of(2025, 11, 9, 12, 0),
            memo = "これは見えないはず",
            image = null,
            recipeId = null
        )

        coEvery { mealRepository.findById(mealId) } returns otherUserMeal

        val input = GetMealInput(
            userId = userId,
            mealId = mealId
        )

        // When
        val result = useCase.execute(input)

        // Then
        result.isLeft() shouldBe true
        val error = result.leftOrNull()!!
        error.shouldBeInstanceOf<GetMealUseCaseErrors.Forbidden>()
        error.code shouldBe ErrorCode.FORBIDDEN
    }
})
