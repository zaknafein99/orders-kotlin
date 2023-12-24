package com.kotlin.orders.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
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
@PropertySource("classpath:users.properties")
class SpringSecConfig {

    @Autowired
    private lateinit var environment: Environment

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
            .username(environment.getProperty("user.username"))
            .password(passwordEncoder.encode(environment.getProperty("user.password")))
            .roles(environment.getProperty("user.roles"))
            .build()

        val adminDetails = User.builder()
            .username(environment.getProperty("admin.username"))
            .password(passwordEncoder.encode(environment.getProperty("admin.password")))
            .roles(environment.getProperty("admin.roles"))
            .build()

        return InMemoryUserDetailsManager(userDetails, adminDetails)
    }

}