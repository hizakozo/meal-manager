package com.ken_stack.meal_manager_api.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono
import java.time.Instant

@TestConfiguration
class TestSecurityConfig {

    @Bean
    @Primary
    fun testSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { it.anyExchange().permitAll() }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .build()
    }

    @Bean
    @Primary
    fun testJwtDecoder(): ReactiveJwtDecoder {
        return ReactiveJwtDecoder { token ->
            // トークンの値に応じて異なるsubjectを返す
            val subject = when (token) {
                "other-user-token" -> "auth0|other-user"
                else -> "auth0|test-user-123"
            }

            val jwt = Jwt.withTokenValue(token)
                .header("alg", "none")
                .subject(subject)
                .claim("aud", listOf("https://api.meal-manager.com"))  // テスト用のaudience
                .issuer("https://test.auth0.com/")  // テスト用のissuer
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()
            Mono.just(jwt)
        }
    }
}
