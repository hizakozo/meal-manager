package com.kenstack.mealmanager.feature.meal.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenstack.mealmanager.feature.meal.api.getMeals
import com.kenstack.mealmanager.feature.meal.model.MealListState
import com.kenstack.mealmanager.infrastructure.auth.SecureStorage
import com.kenstack.mealmanager.infrastructure.auth.TokenManager
import com.kenstack.mealmanager.infrastructure.http.HttpClientFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Meal一覧画面のViewModel（Android専用）
 */
class MealListViewModel(
    context: Context
) : ViewModel() {

    private val secureStorage = SecureStorage(context)
    private val tokenManager = TokenManager(secureStorage)

    // TODO: BaseURLは環境変数や設定ファイルから取得するように変更する
    private val httpClient = HttpClientFactory.create(
        baseUrl = "https://api.meal-manager.com", // 仮のURL
        getAccessToken = { tokenManager.getAccessToken() }
    )

    private val _state = MutableStateFlow<MealListState>(MealListState.Loading)
    val state: StateFlow<MealListState> = _state.asStateFlow()

    init {
        loadMeals()
    }

    /**
     * Meal一覧を読み込み
     */
    fun loadMeals() {
        viewModelScope.launch {
            _state.value = MealListState.Loading
            try {
                val meals = getMeals(httpClient)
                _state.value = MealListState.Success(meals)
            } catch (e: Exception) {
                _state.value = MealListState.Error(
                    message = e.message ?: "Failed to load meals"
                )
            }
        }
    }

    /**
     * リフレッシュ
     */
    fun refresh() {
        loadMeals()
    }

    override fun onCleared() {
        super.onCleared()
        httpClient.close()
    }
}
