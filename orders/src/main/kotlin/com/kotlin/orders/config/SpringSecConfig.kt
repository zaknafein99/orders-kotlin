package com.kotlin.orders.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SpringSecConfig {

    @Bean
    fun filterchain(http: HttpSecurity): SecurityFilterChain{
        http.authorizeHttpRequests()
            .anyRequest().authenticated()
            .and().httpBasic(Customizer.withDefaults())
            .csrf().ignoringRequestMatchers("/orders/**")
        return http.build()
    }

}