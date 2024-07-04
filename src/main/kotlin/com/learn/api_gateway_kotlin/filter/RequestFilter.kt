package com.learn.api_gateway_kotlin.filter

import com.learn.api_gateway_kotlin.model.Company
import com.learn.api_gateway_kotlin.model.Student
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class RequestFilter : GatewayFilter {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val body = exchange.getAttribute<Any>("cachedRequestBodyObject")
        println("in request filter")
        if (body is Student) {
            println("body:$body")
        } else if (body is Company) {
            println("body:$body")
        }
        return chain.filter(exchange)
    }
}