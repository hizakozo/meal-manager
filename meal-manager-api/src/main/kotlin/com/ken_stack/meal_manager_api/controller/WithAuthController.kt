package com.ken_stack.meal_manager_api.controller

import com.ken_stack.meal_manager_api.domain.model.Auth0Sub
import com.ken_stack.meal_manager_api.domain.model.User
import com.ken_stack.meal_manager_api.domain.model.UserId
import com.ken_stack.meal_manager_api.domain.repository.UserRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt

/**
 * JWT認証を使用するコントローラーの基底クラス
 */
abstract class WithAuthController(
    private val userRepository: UserRepository
) {

    /**
     * JWTからAuth0 subを取得し、ユーザーIDを返す
     * ユーザーが存在しない場合は自動的に作成する
     */
    protected suspend fun getCurrentUserId(): UserId {
        // JWT認証からAuth0 subを取得
        val auth0Sub = ReactiveSecurityContextHolder.getContext()
            .awaitSingleOrNull()
            ?.authentication
            ?.principal as? Jwt
            ?: throw IllegalStateException("認証情報が取得できません")

        val subValue = auth0Sub.subject

        // Auth0 subからユーザーを検索または作成
        val auth0SubObj = Auth0Sub.create(subValue).getOrNull()
            ?: throw IllegalStateException("不正なAuth0 sub: $subValue")

        val user = userRepository.findByAuth0Sub(auth0SubObj)
            ?: User.create(subValue).getOrNull()?.let { userRepository.save(it) }
            ?: throw IllegalStateException("ユーザーの作成に失敗しました")

        return user.userId
    }
}