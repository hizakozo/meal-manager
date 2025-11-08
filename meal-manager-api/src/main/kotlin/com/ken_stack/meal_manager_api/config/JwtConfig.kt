package com.ken_stack.meal_manager_api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager

@Configuration
class ReactiveAuthenticationManagerConfig(
    @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") private val jwkSetUri: String,
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") private val issuerUri: String,
    @Value("\${auth0.audience}") private val audience: String
) {
    // Auth0のJWTを検証する認証マネージャー（Issuer + Audience検証付き）
    @Bean
    fun auth0AuthenticationManager(): ReactiveAuthenticationManager {
        val jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build()

        // Issuer検証
        val withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri)

        // Audience検証
        val withAudience = AudienceValidator(audience)

        // 両方の検証を組み合わせる
        val validators = DelegatingOAuth2TokenValidator(withIssuer, withAudience)
        jwtDecoder.setJwtValidator(validators)

        return JwtReactiveAuthenticationManager(jwtDecoder)
    }
}

/**
 * Audience検証用のカスタムValidator
 * JWTのaud（audience）クレームに指定された値が含まれているかチェック
 */
class AudienceValidator(private val audience: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        return if (jwt.audience.contains(audience)) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(
                OAuth2Error("invalid_token", "Required audience not found", null)
            )
        }
    }
}
