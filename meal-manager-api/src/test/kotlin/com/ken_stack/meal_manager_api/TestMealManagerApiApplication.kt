package com.ken_stack.meal_manager_api

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<MealManagerApiApplication>().with(TestcontainersConfiguration::class).run(*args)
}
