package com.ken_stack.meal_manager_api.gateway

import com.ken_stack.meal_manager_api.domain.model.Image
import com.ken_stack.meal_manager_api.domain.model.Meal
import com.ken_stack.meal_manager_api.domain.model.UserId
import com.ken_stack.meal_manager_api.domain.repository.MealRepository
import com.ken_stack.meal_manager_api.driver.ImageDbDriver
import com.ken_stack.meal_manager_api.driver.MealDbDriver
import com.ken_stack.meal_manager_api.driver.MealImageDbDriver
import com.ken_stack.meal_manager_api.driver.entities.ImageEntity
import com.ken_stack.meal_manager_api.driver.entities.MealEntity
import com.ken_stack.meal_manager_api.driver.entities.MealImageEntity
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class MealRepositoryImpl(
    private val mealDbDriver: MealDbDriver,
    private val mealImageDbDriver: MealImageDbDriver,
    private val imageDbDriver: ImageDbDriver
) : MealRepository {

    override suspend fun save(meal: Meal): Meal {
        // MealEntityを保存
        val savedMealEntity = mealDbDriver.insert(
            mealId = meal.mealId.value,
            userId = meal.userId.value,
            dishName = meal.dishName.value,
            cookedAt = meal.cookedAt.value,
            memo = meal.memo.value
        ).awaitFirst()

        // Imageがある場合、ImageEntityとMealImageEntityを保存
        meal.image?.let { image ->
            // ImageEntityを保存
            imageDbDriver.insert(
                imageId = image.imageId,
                uploadedAt = image.uploadedAt
            ).awaitFirst()

            // MealImageEntityを保存
            mealImageDbDriver.insert(
                mealId = meal.mealId.value,
                imageId = image.imageId
            ).awaitFirst()
        }

        // 保存されたデータを再取得して返す
        return findById(savedMealEntity.mealId)!!
    }

    override suspend fun findById(mealId: UUID): Meal? {
        // MealEntityを取得
        val mealEntity = mealDbDriver.findById(mealId).awaitFirstOrNull() ?: return null

        // MealImageEntityを取得
        val mealImageEntity = mealImageDbDriver.findById(mealId).awaitFirstOrNull()

        // ImageEntityを取得
        val image = mealImageEntity?.let { mealImage ->
            imageDbDriver.findById(mealImage.imageId).awaitFirstOrNull()?.toDomain()
        }

        // Mealドメインに変換
        return mealEntity.toDomain(image)
    }

    override suspend fun findAll(userId: UserId, startDate: LocalDate?, endDate: LocalDate?): List<Meal> {
        // userIdで絞り込んでMealEntityを取得（SQL時点でフィルタリング）
        val mealEntities = mealDbDriver.findAllByUserId(userId.value).collectList().awaitFirst()

        // 各MealについてImageを取得
        return mealEntities.mapNotNull { mealEntity ->
            val mealImageEntity = mealImageDbDriver.findById(mealEntity.mealId).awaitFirstOrNull()
            val image = mealImageEntity?.let { mealImage ->
                imageDbDriver.findById(mealImage.imageId).awaitFirstOrNull()?.toDomain()
            }
            mealEntity.toDomain(image)
        }.filter { meal ->
            // 日付フィルタリング
            val cookedAt = meal.cookedAt.value.toLocalDate()
            (startDate == null || !cookedAt.isBefore(startDate)) &&
            (endDate == null || !cookedAt.isAfter(endDate))
        }
    }

    // Entity <-> Domain 変換用の拡張関数
    private fun ImageEntity.toDomain(): Image {
        return Image.of(
            imageId = this.imageId,
            uploadedAt = this.uploadedAt
        )
    }

    private fun Image.toEntity(): ImageEntity {
        return ImageEntity(
            imageId = this.imageId,
            uploadedAt = this.uploadedAt
        )
    }

    private fun MealEntity.toDomain(image: Image?): Meal {
        return Meal.of(
            mealId = this.mealId,
            userId = UserId.of(this.userId),
            dishName = this.dishName,
            cookedAt = this.cookedAt,
            memo = this.memo,
            image = image,
            recipeId = null // TODO: recipe対応時に追加
        )
    }

    private fun Meal.toMealEntity(): MealEntity {
        return MealEntity(
            mealId = this.mealId.value,
            userId = this.userId.value,
            dishName = this.dishName.value,
            cookedAt = this.cookedAt.value,
            memo = this.memo.value
        )
    }

    private fun Meal.toMealImageEntity(): MealImageEntity? {
        return this.image?.let {
            MealImageEntity(
                mealId = this.mealId.value,
                imageId = it.imageId
            )
        }
    }
}
