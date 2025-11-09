package com.kenstack.mealmanager.feature.meal.viewmodel

import android.content.Context
import android.util.Log
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

    companion object {
        private const val TAG = "MealListViewModel"
    }

    // TODO: BaseURLは環境変数や設定ファイルから取得するように変更する
    // エミュレーターからlocalhostへアクセスする場合は 10.0.2.2 を使用
    private val httpClient = HttpClientFactory.create(
        baseUrl = "http://10.0.2.2:8080", // ローカルAPIサーバー
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
            Log.d(TAG, "Loading meals from API...")

            try {
                val token = tokenManager.getAccessToken()
                Log.d(TAG, "Access token exists: ${token != null}, length: ${token?.length ?: 0}")

                val meals = getMeals(httpClient)
                Log.d(TAG, "Successfully loaded ${meals.size} meals from API")
                _state.value = MealListState.Success(meals)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load meals from API", e)
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
