package com.learn.api_gateway_kotlin.filter

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

class PostGlobalFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.toString()
        val response = exchange.response
        val request = exchange.request
        val dataBufferFactory = response.bufferFactory()
        val decoratedResponse = getDecoratedResponse(path, response, request, dataBufferFactory)
        return chain.filter(exchange.mutate().response(decoratedResponse).build())
    }

    private fun getDecoratedResponse(
        path: String,
        response: ServerHttpResponse,
        request: ServerHttpRequest,
        dataBufferFactory: DataBufferFactory
    ): ServerHttpResponseDecorator {
        return object : ServerHttpResponseDecorator(response) {
            override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
                println("PRINT PATH : $path")
                if (body is Flux<*>) {
                    val fluxBody = body as Flux<out DataBuffer>

                    return super.writeWith(fluxBody.buffer().handle { dataBuffers, sink ->
                        val joinedBuffers = DefaultDataBufferFactory().join(
                            dataBuffers!!
                        )
                        val content = ByteArray(joinedBuffers.readableByteCount())
                        joinedBuffers.read(content)
                        val responseBody = String(
                            content,
                            StandardCharsets.UTF_8
                        )
                        println("requestId: " + request.id + ", method: " + request.method + ", req url: " + request.uri + ", response body :" + responseBody)
                        try {
                            if (request.uri.path == "/first" && request.method.matches("GET")) {
                                val student: MutableList<*>? =
                                    ObjectMapper().readValue(
                                        responseBody,
                                        MutableList::class.java
                                    )
                                println("student:$student")
                            } else if (request.uri.path == "/second" && request.method
                                    .matches("GET")
                            ) {
                                val companies: MutableList<*>? =
                                    ObjectMapper().readValue(
                                        responseBody,
                                        MutableList::class.java
                                    )
                                println("companies:$companies")
                            }
                        } catch (e: JsonProcessingException) {
                            sink.error(RuntimeException(e))
                            return@handle
                        }
                        sink.next(dataBufferFactory.wrap(responseBody.toByteArray()))
                    }).onErrorResume { err: Throwable ->
                        println("error while decorating Response: {}" + err.message)
                        Mono.empty()
                    }
                }
                return super.writeWith(body)
            }
        }
    }
}