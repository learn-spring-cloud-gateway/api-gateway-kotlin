package com.learn.api_gateway_kotlin.util

import com.learn.api_gateway_kotlin.model.Credential
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AuthUtil(val restTemplate: RestTemplate) {

    fun getToken(username: String?, role: String?): String? {
        val headers = HttpHeaders()
        headers["username"] = username
        headers["role"] = role
        val request: HttpEntity<Credential> = HttpEntity<Credential>(
            Credential("anish", "admin"), headers
        )
        val response = restTemplate.exchange(
            "http://localhost:8088/login",
            HttpMethod.POST,
            request,
            String::class.java
        )
        return response.body
    }
}