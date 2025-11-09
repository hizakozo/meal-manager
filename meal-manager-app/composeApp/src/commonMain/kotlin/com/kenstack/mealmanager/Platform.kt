package com.kenstack.mealmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform