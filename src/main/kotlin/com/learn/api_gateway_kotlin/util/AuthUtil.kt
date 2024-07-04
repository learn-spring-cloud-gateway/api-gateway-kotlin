package com.learn.api_gateway_kotlin.util

import com.learn.api_gateway_kotlin.model.Credential
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AuthUtil {
    @Autowired
    private val restTemplate: RestTemplate? = null

    fun getToken(userName: String?, role: String?): String? {
        val headers = HttpHeaders()
        headers["userName"] = userName
        headers["role"] = role
        val request: HttpEntity<Credential> = HttpEntity<Credential>(
            Credential("anish", "admin"), headers
        )
        val response = restTemplate!!.exchange(
            "http://localhost:8088/login", HttpMethod.POST, request,
            String::class.java
        )
        println("token:" + response.body)
        return response.body
    }
}