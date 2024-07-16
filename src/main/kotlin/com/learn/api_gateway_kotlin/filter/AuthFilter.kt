package com.learn.api_gateway_kotlin.filter

import com.learn.api_gateway_kotlin.util.AuthUtil
import com.learn.api_gateway_kotlin.util.JWTUtil
import com.learn.api_gateway_kotlin.validator.RouteValidator
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*

@Component
@RefreshScope
class AuthFilter(
    val authUtil: AuthUtil,
    val jwtUtil: JWTUtil,
    val routeValidator: RouteValidator
) : GatewayFilter {

    @Value("\${authentication.enabled}")
    private val authEnabled = false

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        if (!authEnabled) {
            println("Authentication is disabled. To enable it, make \"authentication.enabled\" property as true")
            return chain.filter(exchange)
        }
        val token: String
        val request = exchange.request

        if (routeValidator.isSecured.test(request)) {
            println("validating authentication token")
            if (this.isCredsMissing(request)) {
                println("in error")
                return this.onError(exchange, "Credentials missing", HttpStatus.UNAUTHORIZED)
            }
            token = if (request.headers.containsKey("username") && request.headers.containsKey("role")) ({
                authUtil.getToken(
                    Objects.requireNonNull(request.headers["username"]).toString(),
                    Objects.requireNonNull(request.headers["role"]).toString()
                )
            }).toString() else {
                Objects.requireNonNull(request.headers["Authorization"]).toString().split(" ".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1]
            }
            try {
                if (jwtUtil.isInvalid(token)) {
                    return this.onError(exchange, "Auth header invalid", HttpStatus.UNAUTHORIZED)
                }
            }
            catch (e: ExpiredJwtException){
                println(e)
                return this.onError(exchange, e.message ?: "Expiration of given JWT token", HttpStatus.UNAUTHORIZED)
            }

            this.populateRequestWithHeaders(exchange, token)
        }
        return chain.filter(exchange)
    }

    private fun onError(exchange: ServerWebExchange, err: String, httpStatus: HttpStatus): Mono<Void> {
        val response = exchange.response
        response.setStatusCode(httpStatus)
        return response.setComplete()
    }

    private fun isCredsMissing(request: ServerHttpRequest): Boolean {
        return !(request.headers.containsKey("username") && request.headers.containsKey("role")) && !request.headers.containsKey(
            "Authorization"
        )
    }

    private fun populateRequestWithHeaders(exchange: ServerWebExchange, token: String) {
        val claims: Claims = jwtUtil.getALlClaims(token)
        exchange.request
            .mutate()
            .header("id", claims["id"].toString())
            .header("role", claims["role"].toString())
            .build()
    }
}