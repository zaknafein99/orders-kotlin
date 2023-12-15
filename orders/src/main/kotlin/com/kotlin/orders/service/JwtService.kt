package com.kotlin.orders.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@Service
class JwtService {

    companion object {
        const val SECRET_KEY = "7rfxl9mKL6i1teFzVdql8zCcgwUrM06A"
    }
    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims: Claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    fun generateToken(userDetails: UserDetails): String {
        return generateToken(emptyMap(), userDetails)
    }

    fun generateToken(extraClaims: Map<String, Any>, userDetails: UserDetails): String {
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
            .signWith(getSignInKey(), SignatureAlgorithm.ES256)
            .compact()
    }

    fun extractAllClaims(token: String): Claims {
        return Jwts
            .parser()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun getSignInKey(): Key {
        val keyBytes: ByteArray = Base64.getDecoder().decode(SECRET_KEY)
        return Keys.hmacShaKeyFor(keyBytes)
    }

}
