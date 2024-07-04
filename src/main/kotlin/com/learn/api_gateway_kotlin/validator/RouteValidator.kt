package com.learn.api_gateway_kotlin.validator

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import java.util.function.Predicate

@Component
class RouteValidator {
    var isSecured: Predicate<ServerHttpRequest> =
        Predicate { request: ServerHttpRequest ->
            unprotectedURLs.stream().noneMatch { uri: String? ->
                request.uri.path.contains(uri!!)
            }
        }

    companion object {
        val unprotectedURLs: List<String> = listOf("/login")
    }
}