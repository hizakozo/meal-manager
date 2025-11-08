package com.ken_stack.meal_manager_api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebSecurityConfig(
    private val reactiveAuthenticationManagerConfig: ReactiveAuthenticationManagerConfig,
) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .authorizeExchange { exchange ->
                exchange.anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                // Auth0のJWTのみを使用
                oauth2.authenticationManagerResolver(authenticationManagerResolver())
            }
            .build()
    }

    @Bean
    fun authenticationManagerResolver(): ReactiveAuthenticationManagerResolver<ServerWebExchange> {
        val auth0Manager = reactiveAuthenticationManagerConfig.auth0AuthenticationManager()

        // 全てのリクエストでAuth0認証を使用
        return ReactiveAuthenticationManagerResolver { _ ->
            Mono.just(auth0Manager)
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val c = CorsConfiguration()
        c.addAllowedMethod(CorsConfiguration.ALL)
        c.addAllowedHeader(CorsConfiguration.ALL)
        c.allowCredentials = true
        c.addAllowedOriginPattern("*") // Allow all origins with a pattern
        c.applyPermitDefaultValues()
        c.addExposedHeader("location")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", c)
        return source
    }
}
