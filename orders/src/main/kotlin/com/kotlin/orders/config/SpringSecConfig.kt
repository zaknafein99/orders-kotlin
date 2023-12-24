package com.kotlin.orders.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SpringSecConfig {

    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeRequests {
                authorize("/orders", hasAuthority("SCOPE_orders.read"))
                authorize(anyRequest, authenticated)
            }
            formLogin{
                loginPage = "/login"
            }
        }
        return http.build()
    }
    @Bean
    fun userDetailsService(): UserDetailsService {
        val passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
        val userDetails = User.builder()
            .username("user")
            .password(passwordEncoder.encode("password"))
            .roles("USER")
            .build()

        val adminDetails = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("adminpassword"))
            .roles("ADMIN")
            .build()
        return InMemoryUserDetailsManager(userDetails, adminDetails)
    }

}