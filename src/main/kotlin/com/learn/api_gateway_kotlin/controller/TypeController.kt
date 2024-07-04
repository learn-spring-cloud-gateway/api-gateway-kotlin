package com.learn.api_gateway_kotlin.controller

import com.learn.api_gateway_kotlin.model.Company
import com.learn.api_gateway_kotlin.model.Student
import com.learn.api_gateway_kotlin.model.Type
import com.learn.api_gateway_kotlin.util.AuthUtil
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/process")
class TypeController(
    val restTemplate: RestTemplate,
    val authUtil: AuthUtil
) {
    @PostMapping
    fun getType(@RequestBody type: Type, inRequest: ServerHttpRequest): String {
        println("getting type")
        println("types:" + type.types)
        type.types.forEach { f ->
            if (f == "Student") {
                println("calling first microservice - student")
                val request: HttpEntity<Student> = HttpEntity<Student>(
                    Student(1, "Test", "Student"),
                    setAuthHeader(
                        inRequest.headers["userName"].toString(),
                        inRequest.headers["role"].toString()
                    )
                )
                restTemplate.exchange(
                    "http://localhost:8080/first", HttpMethod.POST, request,
                    String::class.java
                )
            }
            if (f.equals("Company")) {
                println("calling second microservice - company")
                val request: HttpEntity<Company> = HttpEntity<Company>(
                    Company(1, "Test", "Company"),
                    setAuthHeader(
                        inRequest.headers["username"].toString(),
                        inRequest.headers["role"].toString()
                    )
                )
                restTemplate.exchange(
                    "http://localhost:8080/second", HttpMethod.POST, request,
                    String::class.java
                )
            }
        }
        return "done"
    }

    private fun setAuthHeader(userName: String, role: String): HttpHeaders {
        val headers = HttpHeaders()
        headers["Authorization"] = "Bearer " + authUtil.getToken(userName, role)
        return headers
    }
}