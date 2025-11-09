package com.kenstack.mealmanager.navigation

/**
 * 画面のルート定義
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object MealList : Screen("meal_list")
    data object MealDetail : Screen("meal_detail/{mealId}") {
        fun createRoute(mealId: String) = "meal_detail/$mealId"
    }
    data object MealCreate : Screen("meal_create")
}
