package com.kotlin.orders.service

import com.kotlin.orders.config.JwtProperties
import com.kotlin.orders.controller.auth.AuthenticationRequest
import com.kotlin.orders.controller.auth.AuthenticationResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthenticationService(
        private val authManager: AuthenticationManager,
        private val userDetailsService: CustomUserDetailsService,
        private val tokenService: TokenService,
        private val jwtProperties: JwtProperties
) {
    fun authentication(authRequest: AuthenticationRequest): AuthenticationResponse {
        authManager.authenticate(
                UsernamePasswordAuthenticationToken(
                        authRequest.email,
                        authRequest.password
                )
        )

        val foundUser = userDetailsService.loadUserByUsername(authRequest.email)
        val accessToken = tokenService.generate(
                userDetails = foundUser,
                expirationDate = Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration)
        )

        return AuthenticationResponse(
                accessToken = accessToken
        )

    }




}
