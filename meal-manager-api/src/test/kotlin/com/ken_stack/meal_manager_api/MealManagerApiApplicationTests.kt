package com.ken_stack.meal_manager_api

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class MealManagerApiApplicationTests {

	@Test
	fun contextLoads() {
	}

}
