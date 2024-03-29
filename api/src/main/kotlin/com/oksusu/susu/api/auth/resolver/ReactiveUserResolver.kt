package com.oksusu.susu.api.auth.resolver

import com.oksusu.susu.api.auth.application.AuthFacade
import com.oksusu.susu.api.auth.model.AUTH_TOKEN_KEY
import com.oksusu.susu.api.auth.model.AdminUser
import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.auth.model.AuthUserToken
import org.springframework.core.MethodParameter
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolverSupport
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class ReactiveUserResolver(
    adapterRegistry: ReactiveAdapterRegistry,
    private val authFacade: AuthFacade,
) : HandlerMethodArgumentResolverSupport(adapterRegistry) {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == AuthUser::class.java || parameter.parameterType == AdminUser::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange,
    ): Mono<Any> {
        val tokenMono = resolveToken(exchange.request)

        return if (parameter.parameterType == AuthUser::class.java) {
            authFacade.resolveAuthUser(tokenMono)
        } else {
            authFacade.resolveAdminUser(tokenMono)
        }
    }

    private fun resolveToken(request: ServerHttpRequest): Mono<AuthUserToken> {
        val authUserToken = request.headers
            .asSequence()
            .filter { header -> isTokenHeader(header.key) }
            .mapNotNull { header ->
                header.value
                    .firstOrNull()
                    ?.takeIf { token -> token.isNotBlank() }
                    ?.let { token -> AuthUserToken.from(token) }
            }.firstOrNull() ?: AuthUserToken.from("")

        return authUserToken.toMono()
    }

    private fun isTokenHeader(headerKey: String): Boolean {
        return AUTH_TOKEN_KEY.equals(headerKey, ignoreCase = true)
    }
}
