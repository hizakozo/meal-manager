package com.ken_stack.meal_manager_api

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ActiveProfiles("local")
class MealManagerApiApplicationTests {

	@Test
	fun contextLoads() {
	}

}
