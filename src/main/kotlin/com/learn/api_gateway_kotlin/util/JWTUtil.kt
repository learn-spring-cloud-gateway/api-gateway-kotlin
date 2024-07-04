package com.learn.api_gateway_kotlin.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JWTUtil {
    @Value("\${jwt.secret}")
    private val secret: String? = null

    fun getALlClaims(token: String?): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun isTokenExpired(token: String): Boolean {
        return getALlClaims(token).expiration.before(Date())
    }

    fun isInvalid(token: String): Boolean {
        return this.isTokenExpired(token)
    }
}