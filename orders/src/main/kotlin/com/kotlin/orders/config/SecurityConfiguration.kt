package com.kotlin.orders.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
        private val authenticationProvider: AuthenticationProvider
) {

    @Bean
    fun securityFilterChain(
            http: HttpSecurity,
            jwtAuthenticationFilter: JwtAuthenticationFilter
    ): DefaultSecurityFilterChain =
            http
                .csrf{ it.disable() }
                .cors { it.disable() }
                .authorizeHttpRequests{
                    it
                            .requestMatchers(
                                "/auth", 
                                "/auth/refresh", 
                                "/error",
                                "/customer/**",
                                "/orders/**"
                            ).permitAll()
                            .requestMatchers(HttpMethod.POST, "/user").permitAll()
                            .requestMatchers("/user**").hasRole("ADMIN")
                            .anyRequest().fullyAuthenticated()
                }
                .sessionManagement{
                    it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                }
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
                .build()


}